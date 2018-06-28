package com.pixelpalette;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class PixelApp extends ApplicationAdapter implements InputProcessor {
	private DrawStage drawStage;

	public static float height, width, ppu, pixelSize;
	public static final AssetManager MANAGER = new AssetManager();
	public static PlatformOperations platformOperations;

	public PixelApp(final PlatformOperations platformOperations)
	{
		super();
		PixelApp.platformOperations = platformOperations;
	}

	@Override
	public void create()
	{
		MANAGER.load("Paper.png", Pixmap.class);
		MANAGER.load("GUI.png", Texture.class);
		MANAGER.load("Menu.png", Texture.class);
		MANAGER.finishLoading();

		height = 10; // Change as needed
		ppu = Gdx.graphics.getHeight() / height;
		width = Gdx.graphics.getWidth() / ppu;
		Camera camera = new OrthographicCamera(width, height);
		camera.position.set(width / 2, height / 2, 0);
		camera.update();
		FitViewport viewport = new FitViewport(width, height, camera);
		pixelSize = 1 / 18f;

		drawStage = new DrawStage(viewport);

		if(!Gdx.files.external("PixelApp/Projects/.nomedia").exists()) {
			FileHandle nomedia = Gdx.files.external("PixelApp/Projects/.nomedia");
			nomedia.writeString("nomedia", true);
		}
		if(!Gdx.files.external("PixelApp/Clipboard/.nomedia").exists()) {
			FileHandle nomedia = Gdx.files.external("PixelApp/Clipboard/.nomedia");
			nomedia.writeString("nomedia", true);
		}

		InputMultiplexer im = new InputMultiplexer(drawStage, this); // Add in more stuff later
		Gdx.input.setInputProcessor(im);
		Gdx.input.setCatchBackKey(true);
	}

	@Override
	public void render()
	{
		if (drawStage.getCanvas().isMoving())
		{
			Gdx.gl.glClearColor(0, 0.75f, 0.25f, 1);
		}
		else if (drawStage.getMaskVisible())
		{
			Gdx.gl.glClearColor(0.75f, 0, 0, 1);
		}
		else
		{
			Gdx.gl.glClearColor(1f, 0.33f, 0, 1);
		}
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		drawStage.act();
		drawStage.draw();
	}
	
	@Override
	public void dispose () {
		MANAGER.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		boolean ret = false;
		switch (keycode) {
			case Input.Keys.BACK:
				drawStage.getMenu().setMinimized(!drawStage.getMenu().getMinimized());
				ret = true;
				break;
			default:
				break;
		}
		return ret;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}
}
