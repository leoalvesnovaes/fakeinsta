package com.example.instagram.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram.R;
import com.example.instagram.adapter.AdapterMiniaturas;
import com.example.instagram.helper.ConfiguracaoFireBase;
import com.example.instagram.helper.RecyclerItemClickListener;
import com.example.instagram.helper.UsuarioFirebase;
import com.example.instagram.model.Postagem;
import com.example.instagram.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class FiltroActivity extends AppCompatActivity {

    static {
        System.loadLibrary("NativeImageProcessor");
    }

    private ImageView imageFotoEscolhida;
    private Bitmap imagem;
    private Bitmap imagemFiltro;
    private List<ThumbnailItem> listaFiltros;
    private String idUsuarioLogado;
    private TextInputEditText textDescricaoFiltro;
    private Usuario usuarioLogado;
    private RecyclerView recyclerFiltros;
    private AdapterMiniaturas adapterMiniaturas;
    private DatabaseReference usuariosRef;
    private DatabaseReference usuarioLogadoRef;
    private DatabaseReference firebaseRef;
    private DataSnapshot seguidoresSnapshot;
    private AlertDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtro);

        //configuracoes iniciais
        listaFiltros = new ArrayList<>();
        firebaseRef = ConfiguracaoFireBase.getFirebase();
        idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuario();
        usuariosRef = ConfiguracaoFireBase.getFirebase().child("usuarios");


        //inicializa componentes
        imageFotoEscolhida = findViewById(R.id.imageFotoEscolhida);
        recyclerFiltros = findViewById(R.id.recyclerFiltros);
        textDescricaoFiltro = findViewById(R.id.textDescricaoFiltro);


        //recuperar dados para uma nova postagem
        recuperarDadosPostagem();

        //configuração toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Filtros");
        setSupportActionBar(toolbar);
        //botão voltar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);


        //recupera a imagem escolhida pelo usuario
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            byte[] dadosImagem = bundle.getByteArray("fotoEscolhida");
            imagem = BitmapFactory.decodeByteArray(dadosImagem, 0, dadosImagem.length);
            imageFotoEscolhida.setImageBitmap(imagem);
            imagemFiltro = imagem.copy(imagem.getConfig(), true);

            //configura recyclerview de filtros
            adapterMiniaturas = new AdapterMiniaturas(listaFiltros, getApplicationContext());
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            recyclerFiltros.setLayoutManager(layoutManager);
            recyclerFiltros.setAdapter(adapterMiniaturas);

            //adiciona o evento de clieque no recyclerview
            recyclerFiltros.addOnItemTouchListener(
                    new RecyclerItemClickListener(
                            getApplicationContext(),
                            recyclerFiltros,
                            new RecyclerItemClickListener.OnItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {

                                    ThumbnailItem item = listaFiltros.get(position);

                                    imagemFiltro = imagem.copy(imagem.getConfig(), true);
                                    Filter filtro = item.filter;
                                    imageFotoEscolhida.setImageBitmap(filtro.processFilter(imagemFiltro));
                                }

                                @Override
                                public void onLongItemClick(View view, int position) {

                                }

                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                }
                            }
                    )
            );

            //recupera filtros
            recuperarFiltros();

        }

    }


    private void abrirDialogCarregamento(String titulo) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(titulo);
        alert.setCancelable(false);
        alert.setView(R.layout.carregamento);

        dialog = alert.create();
        dialog.show();
    }


    private void recuperarDadosPostagem() {

        abrirDialogCarregamento("Carregando dados, aguarde!");
        usuarioLogadoRef = usuariosRef.child(idUsuarioLogado);
        usuarioLogadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //recupera dados do usuario logado
                usuarioLogado = dataSnapshot.getValue(Usuario.class);

//                recuperar os seguidores
                DatabaseReference seguidoresRef = firebaseRef
                        .child("seguidores")
                        .child(idUsuarioLogado);
                seguidoresRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        seguidoresSnapshot = dataSnapshot;
                        dialog.cancel();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void recuperarFiltros() {

        //limpar itens
        ThumbnailsManager.clearThumbs();
        listaFiltros.clear();

        //configurar filtro normal
        ThumbnailItem item = new ThumbnailItem();
        item.image = imagem;
        item.filterName = "Normal";
        ThumbnailsManager.addThumb(item);

        //listar todos os filtros
        List<Filter> filtros = FilterPack.getFilterPack(getApplicationContext());
        for (Filter filtro : filtros) {

            ThumbnailItem itemFiltro = new ThumbnailItem();
            itemFiltro.image = imagem;
            itemFiltro.filter = filtro;
            itemFiltro.filterName = filtro.getName();

            ThumbnailsManager.addThumb(itemFiltro);
        }

        listaFiltros.addAll(ThumbnailsManager.processThumbs(getApplicationContext()));
        adapterMiniaturas.notifyDataSetChanged();


    }


    private void publicarPostagem() {

        abrirDialogCarregamento("Salvando postagem");
        final Postagem postagem = new Postagem();
        postagem.setIdUsuario(idUsuarioLogado);
        postagem.setDescricao(textDescricaoFiltro.getText().toString());

        //recuperar dados da imagem para o firebase
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imagemFiltro.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] dadosImagem = baos.toByteArray();

        //salvar a imagem no fire storage
        StorageReference storageRef = ConfiguracaoFireBase.getFirebaseStorage();
        StorageReference imagemRef = storageRef
                .child("imagens")
                .child("postagens")
                .child(postagem.getId() + ".jpeg");

        UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FiltroActivity.this,
                        "Problemas, deu erro ao fazer postagem",
                        Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                //recuperar local da foto
                Uri url = taskSnapshot.getDownloadUrl();
                postagem.setCaminhoFoto(url.toString());

                //atualizar a qtd da postagem
                int qtdPostagem = usuarioLogado.getPostagens() + 1;
                usuarioLogado.setPostagens(qtdPostagem);
                usuarioLogado.atualizarQtdPostagem();

                //salvar postagem
                if (postagem.salvar(seguidoresSnapshot)) {

                    Toast.makeText(FiltroActivity.this,
                            "Show, você fez um Post!",
                            Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                    finish();
                }

            }
        });


    }


    //criando o menu postagem
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_filtro, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.ic_salvar_postagem:
                publicarPostagem();

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //para funcionar o botão de fechar activty
    @Override
    public boolean onSupportNavigateUp() {

        finish();
        return false;
    }
}