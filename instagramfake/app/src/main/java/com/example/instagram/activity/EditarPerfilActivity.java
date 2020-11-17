package com.example.instagram.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.instagram.R;
import com.example.instagram.helper.ConfiguracaoFireBase;
import com.example.instagram.helper.Permissao;
import com.example.instagram.helper.UsuarioFirebase;
import com.example.instagram.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditarPerfilActivity extends AppCompatActivity {

    private CircleImageView imageEditarPerfil;
    private TextView textAlterarFoto;
    private TextInputEditText editNomePerfil, editEmailPerfil;
    private Button buttonSalvarAlteracoes;
    private Usuario usuarioLogado;
    private static final int SELECAO_GALERIA = 200;
    private StorageReference storageRef;
    private String identificadorUsuario;

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        //validar permissoes
        Permissao.validarPermissoes(permissoesNecessarias, this, 1);

        //configurações iniciais
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        storageRef = ConfiguracaoFireBase.getFirebaseStorage();
        identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();

        //configuração toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Editar Perfil");
        setSupportActionBar(toolbar);
        //botão voltar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);

        //inicializando componentes
        inicializarComponentes();

        //recuperar os dados
        FirebaseUser usuarioPerfil = UsuarioFirebase.getUsuarioAtual();
        editNomePerfil.setText(usuarioPerfil.getDisplayName().toUpperCase());
        editEmailPerfil.setText(usuarioPerfil.getEmail());
        //recuperar foto
        Uri url = usuarioPerfil.getPhotoUrl();
        if (url != null) {
            Glide.with(EditarPerfilActivity.this)
                    .load(url)
                    .into(imageEditarPerfil);
        } else {
            imageEditarPerfil.setImageResource(R.drawable.avatar2);
        }

        //salvar alteração do nome
        buttonSalvarAlteracoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nomeAtualizado = editNomePerfil.getText().toString();

                //atualizar nome do perfil do fire
                UsuarioFirebase.atualizarNomeUsuario(nomeAtualizado);

                //atualizar o nome no banco de dados
                usuarioLogado.setNome(nomeAtualizado);
                usuarioLogado.atualizar();

                Toast.makeText(EditarPerfilActivity.this,
                        "Dados Alterados com Xuuuucesso!",
                        Toast.LENGTH_SHORT).show();
            }
        });

        //alterar foto do usuario
        textAlterarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (i.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(i, SELECAO_GALERIA);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Bitmap imagem = null;

            try {

                //selecao apenas da galeria
                switch (requestCode) {
                    case SELECAO_GALERIA:
                        Uri localImagemSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);
                        break;
                }

                //caso tenha sido escolhido uma imagem
                if (imagem != null) {

                    //configura imagem na tela
                    imageEditarPerfil.setImageBitmap(imagem);

                    //recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //salvar imagem no firebase
                    StorageReference imagemRef = storageRef
                            .child("imagens")
                            .child("perfil")
                            .child(identificadorUsuario + ".jpeg");

                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditarPerfilActivity.this,
                                    "Aow carai, deu erro ao fazer upload da imagem",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            //recuperar local da foto
                            Uri url = taskSnapshot.getDownloadUrl();
                            atualizarFotoUsuario(url);

                            Toast.makeText(EditarPerfilActivity.this,
                                    "Xuuuucesso ao fazer upload da imagem maluuuco",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void atualizarFotoUsuario(Uri url) {

        //atualizar foto no perfil
        UsuarioFirebase.atualizarFotoUsuario(url);

        //atualizar foto no firebase
        usuarioLogado.setCaminhoFoto(url.toString());
        usuarioLogado.atualizar();

        Toast.makeText(EditarPerfilActivity.this,
                "Xuuuucesso, foto atualizada",
                Toast.LENGTH_SHORT).show();

    }

    public void inicializarComponentes() {

        imageEditarPerfil = findViewById(R.id.imageEditarPerfil);
        textAlterarFoto = findViewById(R.id.textAlterarFoto);
        editNomePerfil = findViewById(R.id.editNomePerfil);
        editEmailPerfil = findViewById(R.id.editEmailPerfil);
        buttonSalvarAlteracoes = findViewById(R.id.buttonSalvarAlteracoes);
        editEmailPerfil.setFocusable(false);

    }

    //voltar ao fragment perfil
    @Override
    public boolean onSupportNavigateUp() {

        finish();
        return false;
    }
}