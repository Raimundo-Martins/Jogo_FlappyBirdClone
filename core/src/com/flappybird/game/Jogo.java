package com.flappybird.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class Jogo extends ApplicationAdapter {

	private SpriteBatch batch;
	private Texture[] texturePassaros;
	private Texture textureFundo0;
	private Texture textureFundo1;
	private Texture textureFundo2;
	private Texture textureCanoTopo;
	private Texture textureCanoAbaixo;
	private Texture textureGameOver;

	private float larguraTela;
	private float alturaTela;
	private float variacao = 0;
	private float gravidade = 0;
	private float posicaoInicialPassaro = 0;
	private float posicaoCanoHorizontal;
	private float posicaoCanoVertical;
	private float espacoCanos;
	private float animacaoImagemFundo = 0;

	private Random randomAleatorio;
	private int pontos = 0;
	private int pontuacaoMaxima = 0;
	private boolean canoPassou = false;
	private int estadoJogo = 0;

	private ShapeRenderer shapeRenderer;
	private Circle circlePassaro;
	private Rectangle rectangleCanoTopo;
	private Rectangle rectangleCanoAbaixo;

	private OrthographicCamera orthographicCamera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 720;
	private final float VIRTUAL_HEIGHT = 1280;

	BitmapFont bitmapFontPontuacao;
	BitmapFont bitmapFontReiniciar;
	BitmapFont bitmapFontMelhorPontuacao;

	Sound soundVoando;
	Sound soundColisao;
	Sound soundPontuacao;

	Preferences preferences;

	@Override
	public void create () {
		inicializarTexteres();
		inicializarObjetos();
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		verificarEstadoJogo();
		validarPontos();
		desenharObjetos();
		detectarColicoes();

	}

	private void inicializarTexteres(){
		texturePassaros = new Texture[3];
		texturePassaros[0] = new Texture("passaro1.png");
		texturePassaros[1] = new Texture("passaro2.png");
		texturePassaros[2] = new Texture("passaro3.png");
		textureFundo0 = new Texture("fundo.png");
		textureFundo1 = new Texture("fundo.png");
		textureFundo2 = new Texture("fundo.png");
		textureCanoTopo = new Texture("cano_topo_maior.png");
		textureCanoAbaixo = new Texture("cano_baixo_maior.png");
		textureGameOver = new Texture("game_over.png");
	}

	private void inicializarObjetos(){
		batch = new SpriteBatch();

		randomAleatorio = new Random();

		bitmapFontPontuacao = new BitmapFont();
		bitmapFontPontuacao.setColor(Color.WHITE);
		bitmapFontPontuacao.getData().setScale(10);

		bitmapFontReiniciar = new BitmapFont();
		bitmapFontReiniciar.setColor(Color.GREEN);
		bitmapFontReiniciar.getData().setScale(3);

		bitmapFontMelhorPontuacao = new BitmapFont();
		bitmapFontMelhorPontuacao.setColor(Color.RED);
		bitmapFontMelhorPontuacao.getData().setScale(3);

		shapeRenderer = new ShapeRenderer();

		circlePassaro = new Circle();
		rectangleCanoTopo = new Rectangle();
		rectangleCanoAbaixo = new Rectangle();

		larguraTela = VIRTUAL_WIDTH;
		alturaTela = VIRTUAL_HEIGHT;

		posicaoInicialPassaro = alturaTela / 2;
		posicaoCanoHorizontal = larguraTela;
		espacoCanos = 300;

		soundVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		soundColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		soundPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));

		preferences = Gdx.app.getPreferences("flappyBird");
		pontuacaoMaxima = preferences.getInteger("pontuacaoMaxima", 0);

		orthographicCamera = new OrthographicCamera();
		orthographicCamera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, orthographicCamera);
	}

	private void desenharObjetos(){
		batch.setProjectionMatrix(orthographicCamera.combined);

		batch.begin();

		/*if (posicaoCanoHorizontal < - textureCanoTopo.getWidth()){
			batch.draw(textureFundo0, animacaoImagemFundo, 0, larguraTela - animacaoImagemFundo, alturaTela);
		}*/
		batch.draw(textureFundo1, animacaoImagemFundo, 0, larguraTela, alturaTela);
		batch.draw(textureFundo2, animacaoImagemFundo, 0, larguraTela * 2, alturaTela);
		//batch.draw(textureFundo2, animacaoImagemFundo, 0, larguraTela * 3, alturaTela);
		batch.draw(texturePassaros[(int) variacao], 50, posicaoInicialPassaro);
		batch.draw(textureCanoTopo, posicaoCanoHorizontal, (alturaTela / 2) + (espacoCanos / 2) + posicaoCanoVertical);
		batch.draw(textureCanoAbaixo, posicaoCanoHorizontal, (alturaTela / 2) - textureCanoAbaixo.getHeight() - (espacoCanos / 2) + posicaoCanoVertical);

		bitmapFontPontuacao.draw(batch, String.valueOf(pontos), larguraTela / 2 - bitmapFontPontuacao.getXHeight() / 2, alturaTela - 125);

		if (estadoJogo == 2){
			batch.draw(textureGameOver, (larguraTela / 2) - textureGameOver.getWidth() / 2, alturaTela / 2);
			bitmapFontReiniciar.draw(batch, "Toque para reiniciar", larguraTela / 2 - 175, alturaTela / 2 - textureGameOver.getHeight() / 2);
			bitmapFontMelhorPontuacao.draw(batch, "Seu record é: " + pontuacaoMaxima + " pontos", larguraTela / 2 - 200, alturaTela / 2 - textureGameOver.getHeight() - 25);
		}

		batch.end();
	}

	private void verificarEstadoJogo(){
		boolean toqueTela = Gdx.input.justTouched();

		if (estadoJogo == 0){

			if (toqueTela){
				gravidade = -15;
				estadoJogo = 1;
				soundVoando.play();
			}

		} else if (estadoJogo == 1){
			if (toqueTela){
				gravidade = -15;
				soundVoando.play();
			}

			posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 200;
			animacaoImagemFundo -= Gdx.graphics.getDeltaTime() * 200;

			if (posicaoCanoHorizontal < - textureCanoTopo.getWidth()){
				posicaoCanoHorizontal = larguraTela;
				posicaoCanoVertical = randomAleatorio.nextInt(400) - 200;
				canoPassou = false;
			}

			if (posicaoInicialPassaro > 0 || toqueTela){
				posicaoInicialPassaro = posicaoInicialPassaro - gravidade;
			}

			gravidade++;

		}else if (estadoJogo == 2){

			if (pontos > pontuacaoMaxima){
				pontuacaoMaxima = pontos;
				preferences.putInteger("pontuacaoMaxima", pontuacaoMaxima);
			}

			posicaoInicialPassaro = posicaoInicialPassaro - gravidade;
			gravidade++;

			if (toqueTela){
				estadoJogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaoInicialPassaro = alturaTela / 2;
				posicaoCanoHorizontal = larguraTela;
				animacaoImagemFundo = 0;
			}
		}
	}

	public void validarPontos(){
		if (posicaoCanoHorizontal < 50 - texturePassaros[0].getWidth()){
			if (!canoPassou){
				pontos++;
				canoPassou = true;
				soundPontuacao.play();
			}
		}

		variacao += Gdx.graphics.getDeltaTime() * 10;
		if (variacao > 2){
			variacao = 0;
		}
	}

	private void detectarColicoes(){
		circlePassaro.set(50 + texturePassaros[0].getWidth() / 2,
				posicaoInicialPassaro + texturePassaros[0].getHeight() / 2,
				texturePassaros[0].getWidth() / 2);

		rectangleCanoTopo.set(posicaoCanoHorizontal,
				(alturaTela / 2) + (espacoCanos / 2) + posicaoCanoVertical,
				textureCanoTopo.getWidth(), textureCanoTopo.getHeight());

		rectangleCanoAbaixo.set(posicaoCanoHorizontal,
				(alturaTela / 2) - textureCanoAbaixo.getHeight() - (espacoCanos / 2) + posicaoCanoVertical,
				textureCanoAbaixo.getWidth(), textureCanoAbaixo.getHeight());

		boolean colidiuCanoTopo = Intersector.overlaps(circlePassaro, rectangleCanoTopo);
		boolean colidiuCanoAbaixo = Intersector.overlaps(circlePassaro, rectangleCanoAbaixo);

		if (colidiuCanoTopo || colidiuCanoAbaixo){
			if (estadoJogo == 1){
				soundColisao.play();
				estadoJogo = 2;
			}
		}

		/*shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(Color.RED);

		shapeRenderer.circle(50 + texturePassaros[0].getWidth() / 2,
				posicaoInicialPassaro + texturePassaros[0].getHeight() / 2,
				texturePassaros[0].getWidth() / 2);

		shapeRenderer.rect(posicaoCanoHorizontal,
				(alturaTela / 2) + (espacoCanos / 2) + posicaoCanoVertical,
				textureCanoTopo.getWidth(), textureCanoTopo.getHeight());

		shapeRenderer.rect(posicaoCanoHorizontal,
				(alturaTela / 2) - textureCanoAbaixo.getHeight() - (espacoCanos / 2) + posicaoCanoVertical,
				textureCanoAbaixo.getWidth(), textureCanoAbaixo.getHeight());

		shapeRenderer.end();*/
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	@Override
	public void dispose () {
	}
}
