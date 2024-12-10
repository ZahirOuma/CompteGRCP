package ma.projet.grcp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Button;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import androidx.appcompat.app.AlertDialog;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import ma.projet.grpc.stubs.Compte;
import ma.projet.grpc.stubs.CompteServiceGrpc;
import ma.projet.grpc.stubs.GetAllComptesRequest;
import ma.projet.grpc.stubs.GetAllComptesResponse;
import ma.projet.grpc.stubs.SaveCompteRequest;
import ma.projet.grpc.stubs.SaveCompteResponse;
import ma.projet.grpc.stubs.CompteRequest;
import ma.projet.grpc.stubs.TypeCompte;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "ControleurPrincipal";
    private RecyclerView vueListeComptes;
    private CompteAdapter adaptateurCompte;
    private List<Compte> listeCompte = new ArrayList<>();
    private FloatingActionButton boutonAjoutCompte;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vueListeComptes = findViewById(R.id.recyclerViewComptes);
        boutonAjoutCompte = findViewById(R.id.fabAddCompte);

        adaptateurCompte = new CompteAdapter(listeCompte);
        vueListeComptes.setLayoutManager(new LinearLayoutManager(this));
        vueListeComptes.setAdapter(adaptateurCompte);

        new Thread(() -> recupererComptesDepuisServeur()).start();

        boutonAjoutCompte.setOnClickListener(v -> afficherDialogAjoutCompte());
    }

    private void recupererComptesDepuisServeur() {
        ManagedChannel canal = ManagedChannelBuilder
                .forAddress("10.0.2.2", 9090)
                .usePlaintext()
                .build();

        try {
            CompteServiceGrpc.CompteServiceBlockingStub stub = CompteServiceGrpc.newBlockingStub(canal);
            GetAllComptesRequest requete = GetAllComptesRequest.newBuilder().build();
            GetAllComptesResponse reponse = stub.allComptes(requete);

            runOnUiThread(() -> {
                listeCompte.clear();
                listeCompte.addAll(reponse.getComptesList());
                adaptateurCompte.notifyDataSetChanged();
            });
        } catch (Exception e) {
            Log.e(LOG_TAG, "Erreur lors de la récupération des comptes", e);
        } finally {
            canal.shutdown();
        }
    }

    private void afficherDialogAjoutCompte() {
        View vueDialog = getLayoutInflater().inflate(R.layout.dialog_add_compte, null);

        EditText champMontant = vueDialog.findViewById(R.id.editTextSolde);
        Spinner listeTypeCompte = vueDialog.findViewById(R.id.spinnerTypeCompte);
        Button boutonSauvegarder = vueDialog.findViewById(R.id.btnSaveCompte);

        ArrayAdapter<String> adaptateurType = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"COURANT", "EPARGNE"});
        adaptateurType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        listeTypeCompte.setAdapter(adaptateurType);

        AlertDialog boiteDialog = new AlertDialog.Builder(this)
                .setTitle("Créer un Compte")
                .setView(vueDialog)
                .create();

        boutonSauvegarder.setOnClickListener(v -> {
            String montantSaisi = champMontant.getText().toString().trim();
            String typeSelectionne = listeTypeCompte.getSelectedItem().toString();

            if (!montantSaisi.isEmpty()) {
                double montant = Double.parseDouble(montantSaisi);

                new Thread(() -> {
                    ManagedChannel canal = ManagedChannelBuilder
                            .forAddress("10.0.2.2", 9090)
                            .usePlaintext()
                            .build();

                    try {
                        CompteServiceGrpc.CompteServiceBlockingStub stub = CompteServiceGrpc.newBlockingStub(canal);

                        SaveCompteRequest requete = SaveCompteRequest.newBuilder()
                                .setCompte(
                                        CompteRequest.newBuilder()
                                                .setSolde((float) montant)
                                                .setDateCreation(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                                                .setType(TypeCompte.valueOf(typeSelectionne))
                                                .build()
                                )
                                .build();

                        SaveCompteResponse reponse = stub.saveCompte(requete);

                        GetAllComptesRequest requeteComptes = GetAllComptesRequest.newBuilder().build();
                        GetAllComptesResponse reponseComptes = stub.allComptes(requeteComptes);

                        runOnUiThread(() -> {
                            listeCompte.clear();
                            listeCompte.addAll(reponseComptes.getComptesList());
                            adaptateurCompte.notifyDataSetChanged();
                            boiteDialog.dismiss();
                        });

                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Erreur lors de l'ajout du compte", e);
                    } finally {
                        canal.shutdown();
                    }
                }).start();
            }
        });

        boiteDialog.show();
    }
}
