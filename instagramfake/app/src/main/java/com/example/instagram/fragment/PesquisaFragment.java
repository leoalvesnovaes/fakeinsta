package com.example.instagram.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram.R;
import com.example.instagram.activity.PerfilAmigoActivity;
import com.example.instagram.adapter.AdapterPesquisa;
import com.example.instagram.helper.ConfiguracaoFireBase;
import com.example.instagram.helper.RecyclerItemClickListener;
import com.example.instagram.helper.UsuarioFirebase;
import com.example.instagram.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class PesquisaFragment extends Fragment {

    //se der erro nesse search, alterar importação de biblioteca
    private SearchView searchViewPesquisa;
    private RecyclerView recyclerViewPesquisa;

    private List<Usuario> listaUsuarios;
    private DatabaseReference usuariosRef;
    private AdapterPesquisa adapterPesquisa;

    private String idUsuarioLogado;

    public PesquisaFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pesquisa, container, false);

        searchViewPesquisa = view.findViewById(R.id.searchViewPesquisa);
        recyclerViewPesquisa = view.findViewById(R.id.recyclerViewPesquia);

        //configuracao iniciais
        listaUsuarios = new ArrayList<>();
        usuariosRef = ConfiguracaoFireBase.getFirebase()
                .child("usuarios");
        idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuario();

        //configura recyclerview
        recyclerViewPesquisa.setHasFixedSize(true);
        recyclerViewPesquisa.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapterPesquisa = new AdapterPesquisa(listaUsuarios, getActivity());
        recyclerViewPesquisa.setAdapter(adapterPesquisa);

        //Configurar evento de clique
        recyclerViewPesquisa.addOnItemTouchListener(new RecyclerItemClickListener(
                getActivity(),
                recyclerViewPesquisa,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        Usuario usuarioSelecionado = listaUsuarios.get(position);
                        Intent i = new Intent(getActivity(), PerfilAmigoActivity.class);
                        i.putExtra( "usuarioSelecionado", usuarioSelecionado);
                        startActivity(i);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
        ));

        //configurar searchview
        searchViewPesquisa.setQueryHint("Buscar usuários");
        searchViewPesquisa.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Log.d("onQueryTextChange", "texto digitado"+ query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String textoDigitado = newText.toUpperCase();
                pesquisarUsuarios(textoDigitado);
                return true;
            }
        });

        return view;
    }


    private void pesquisarUsuarios(String texto) {

        //limpar lista
        listaUsuarios.clear();

        //verificar conteudo pesquisa
        if (texto.length() > 0) {
            Query query = usuariosRef.orderByChild("nome")
                    .startAt(texto)
                    .endAt(texto + "\uf8ff");

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    //limpar a lista
                    listaUsuarios.clear();

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {

                        //verifica se é usuário loado e remove da lista
                        Usuario usuario = ds.getValue( Usuario.class );
                        if ( idUsuarioLogado.equals( usuario.getId() ))
                            continue; //reinicia o for

                        // adiciona um comentario na lista
                        listaUsuarios.add(ds.getValue(Usuario.class));
                    }

                    adapterPesquisa.notifyDataSetChanged();;

//                    int total = listaUsuarios.size();
//                    Log.i("totalUsuarios", "total: "+total);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

    }


}