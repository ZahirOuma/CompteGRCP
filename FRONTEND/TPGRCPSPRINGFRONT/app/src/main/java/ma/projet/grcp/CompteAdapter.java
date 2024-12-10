package ma.projet.grcp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ma.projet.grpc.stubs.Compte;

public class CompteAdapter extends RecyclerView.Adapter<CompteAdapter.VueCompte> {

    private List<Compte> listeComptes;

    public CompteAdapter(List<Compte> comptes) {
        this.listeComptes = comptes;
    }

    @NonNull
    @Override
    public VueCompte onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vue = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_compte, parent, false);
        return new VueCompte(vue);
    }

    @Override
    public void onBindViewHolder(@NonNull VueCompte vue, int position) {
        Compte compte = listeComptes.get(position);
        vue.champId.setText("Identifiant: " + compte.getId());
        vue.champSolde.setText("Montant: " + compte.getSolde());
        vue.champDate.setText("Création: " + compte.getDateCreation());
        vue.champType.setText("Catégorie: " + compte.getType());
    }

    @Override
    public int getItemCount() {
        return listeComptes.size();
    }

    static class VueCompte extends RecyclerView.ViewHolder {
        TextView champId, champSolde, champDate, champType;

        public VueCompte(@NonNull View itemView) {
            super(itemView);
            champId = itemView.findViewById(R.id.tvCompteId);
            champSolde = itemView.findViewById(R.id.tvSolde);
            champDate = itemView.findViewById(R.id.tvDateCreation);
            champType = itemView.findViewById(R.id.tvType);
        }
    }
}
