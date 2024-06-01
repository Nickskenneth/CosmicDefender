package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;
import java.util.Timer;

public class Game extends ApplicationAdapter {
	private SpriteBatch batch;
	private int highScore = 0;

	private int bossSpawnedCount = 0;
	private int health = 0;
	private boolean isHealing = false;
	private long lastHealTime = 0;
	private static final long HEAL_DELAY = 3000000000L;

	// Textures
	TextureRegion background;
	private Texture playerTexture;
	private Texture enemyTexture;
	private Texture playerSkillTexture;
	private Texture projectileTexture;
	private Texture bossTexture;
	private Texture skillUpgradeTexture;
	private Texture explosionsTexture;
	private Timer explosionTimer;

	// Hitbox
	private Rectangle projectile;
	private Rectangle player;
	private Rectangle skill;
	private Array<Rectangle> aliens;
	private Array<Rectangle> projectiles;
	private Rectangle boss;
	private Rectangle skillUpgrade;

	// Spawn
	private long lastSpawnTime = 0;
	private OrthographicCamera camera;

	// Score
	private int score = 0;
	private BitmapFont font;
	private int hearts = 3;

	// Projectile state
	private static final long SHOT_DELAY_INITIAL = 1000000000;
	private static final long SHOT_DELAY_DECREASE = 100000000;

	// Projectile state
	private boolean isProjectileActive = false;
	private long lastShotTime = 0;
	private long shotDelay = SHOT_DELAY_INITIAL;

	// Boss state
	private boolean bossSpawned = false;
	private int bossHealth = 10;

	// Skill upgrade state
	private boolean skillUpgradeSpawned = false;
	private boolean skillUpgradeCollected = false;
	private long skillUpgradeTime;
	private static final long SKILL_UPGRADE_DURATION = 10000000000L;
	//update timer for explosion
	private long explosionStartTime;
	private static final float EXPLOSION_DURATION = 1.0f;
	private Array<Rectangle> explosions;
	private Array<Float> explosionStartTimes;

	private boolean gameStarted = false;
	private boolean gameOver = false;
	boolean isPaused = false;
	// Sound effects
	private AbstractSound explodingSound;
	private AbstractSound shootSound;

	@Override
	public void create() {
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 600);
		batch = new SpriteBatch();
		background = new TextureRegion(new Texture(Gdx.files.internal("background.jpg")),800,600);
		playerTexture = new Texture("player.png");
		enemyTexture = new Texture("alien_type1.png");
		projectileTexture = new Texture("projectile.png");
		playerSkillTexture = new Texture("SkillUp.png");
		bossTexture = new Texture("alien_type2.png");
		skillUpgradeTexture = new Texture("SkillUp.png");
		explosionsTexture = new Texture("explosion.png");
		font = new BitmapFont();
		font.getData().setScale(2);

		explodingSound = new ExplodingSound();
		shootSound = new ShootSound();

		player = new Rectangle();
		player.x = 800 / 2 - 64 / 2;
		player.y = 20;
		player.width = 64;
		player.height = 64;

		aliens = new Array<Rectangle>();
		projectiles = new Array<Rectangle>();
		explosions = new Array<Rectangle>();
		explosionStartTimes = new Array<Float>();
		explosionTimer = new Timer();
	}


	private void spawnEnemy() {
		Rectangle alien = new Rectangle();
		alien.y = 600;
		alien.x = MathUtils.random(0, 800 - 64);
		alien.setHeight(64);
		alien.setWidth(64);
		aliens.add(alien);
		lastSpawnTime = TimeUtils.nanoTime();
	}

	private void spawnBoss() {
		boss = new Rectangle();
		boss.x = 800 / 2 - 128 / 2;
		boss.y = 600;
		boss.setWidth(128);
		boss.setHeight(128);
		bossHealth = 10;
		bossSpawned = true;
		if(bossHealth == 0) {
			if (shotDelay >= 99999996) {
				shotDelay = SHOT_DELAY_DECREASE - 2;
			}
		}
	}

	private void spawnProjectile() {
		long currentTime = TimeUtils.nanoTime();
		if (currentTime - lastShotTime >= shotDelay) {
			Rectangle projectile = new Rectangle();
			projectile.y = player.y;
			projectile.x = player.x + 15;
			projectile.setHeight(32);
			projectile.setWidth(32);
			projectiles.add(projectile);
			isProjectileActive = true;
			shootSound.playSound();
			lastShotTime = currentTime;
		}
	}
	private void spawnExplosion(float x, float y) {
		Rectangle explosion = new Rectangle(x, y, explosionsTexture.getWidth(), explosionsTexture.getHeight());
		explosions.add(explosion);
		explosionStartTimes.add((float) TimeUtils.nanoTime());
	}

	private void startExplosionTimer(Explosion explosion) {
		explosion.setStartTime(TimeUtils.nanoTime());
	}

	private boolean isExplosionFinished(float explosionStartTime) {
		float elapsedSeconds = MathUtils.nanoToSec * (TimeUtils.nanoTime() - explosionStartTime);
		return elapsedSeconds >= EXPLOSION_DURATION;
	}

	private void restartGame() {
		score = 0;
		hearts = 3;
		aliens.clear();
		projectiles.clear();
		bossSpawned = false;
		skillUpgradeSpawned = false;
		skillUpgradeCollected = false;
		isProjectileActive = false;
		gameStarted = true;
		gameOver = false;
		bossHealth = 10;
		lastSpawnTime = TimeUtils.nanoTime();
		skillUpgradeTime = 0;
		lastShotTime = 0;
		player.x = 800 / 2 - 64 / 2;
		player.y = 20;
	}

	private void spawnSkillUpgrade() {
		skillUpgrade = new Rectangle();
		skillUpgrade.x = MathUtils.random(0, 800 - 64);
		skillUpgrade.y = 600;
		skillUpgrade.setWidth(64);
		skillUpgrade.setHeight(64);
		skillUpgradeSpawned = true;
	}
	private void healPlayer() {
		long currentTime = TimeUtils.nanoTime();
		if (currentTime - lastHealTime >= HEAL_DELAY && hearts < 3) {
			isHealing = true;
			lastHealTime = currentTime;
			hearts += 1;
			if (hearts > 3) {
				hearts = 3;
			}
		}
	}

	@Override
	public void render() {
		ScreenUtils.clear(0, 0, 0.2f, 0);
		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			isPaused = !isPaused;
		}
		if (isPaused) {
			batch.begin();
			batch.draw(background,0,0,800,600);
			font.draw(batch, "Game is paused", 290, 300);
			font.draw(batch, "Press E to exit", 295, 250);
			if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
				Gdx.app.exit();
			}
			batch.end();
			return;
		}
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(background,0,0,800,600);


		if (score % 250 == 0 && score != 0 && bossSpawnedCount < score / 250) {
			spawnBoss();
			bossSpawnedCount++;
		}
		if (score > highScore) {
			highScore = score;
		}
		font.draw(batch, "High Score: " + highScore, 600, 50);
		if (!gameStarted) {
			font.draw(batch, "Press ENTER to Start", 250, 325);
			if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
				gameStarted = true;
			}
		} else if (!gameOver) {
			batch.draw(playerTexture, player.x, player.y);
			if (Gdx.input.isKeyPressed(Input.Keys.H)) {
				healPlayer();
			}
			font.draw(batch, "Health: " + hearts, 20, 580);
			for (Rectangle alien : aliens) {
				batch.draw(enemyTexture, alien.x, alien.y);
			}
			for (Rectangle projectile : projectiles) {
				batch.draw(projectileTexture, projectile.x, projectile.y);
			}

//   font.draw(batch, score + "", 700, 550);

			if (bossSpawned) {
				batch.draw(bossTexture, boss.x, boss.y);
			}

			if (skillUpgradeSpawned) {
				batch.draw(skillUpgradeTexture, skillUpgrade.x, skillUpgrade.y);
			}

			font.draw(batch, "Score: " + score, 15, 550);
			if (hearts != 0) {
				// Move player
				if (Gdx.input.isKeyPressed(Input.Keys.A)) {
					player.x = player.x - 200 * Gdx.graphics.getDeltaTime() - 1;
				}
				if (Gdx.input.isKeyPressed(Input.Keys.D)) {
					player.x = player.x + 200 * Gdx.graphics.getDeltaTime() + 1;
				}
				if (Gdx.input.isKeyPressed(Input.Keys.S)) {
					player.y = player.y - 200 * Gdx.graphics.getDeltaTime() - 1;
				}
				if (Gdx.input.isKeyPressed(Input.Keys.W)) {
					player.y = player.y + 200 * Gdx.graphics.getDeltaTime() + 1;
				}

				// Bound player within the screen
				if (player.x < 0) {
					player.x = 0;
				}
				if (player.x > 800 - 64) {
					player.x = 800 - 64;
				}
				if (player.y < 0) {
					player.y = 0;
				}
				if (player.y > 600 - 64) {
					player.y = 600 - 64;
				}

				// Spawn aliens
				if (TimeUtils.nanoTime() - lastSpawnTime > 1000000000) {
					spawnEnemy();
				}

				// Spawn projectiles
				if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
					spawnProjectile();
				}

				// Spawn skill upgrade
				if (score % 50 == 0 && score != 0 && !bossSpawned && !skillUpgradeSpawned) {
					spawnSkillUpgrade();
				}



				// Update boss position
				if (bossSpawned) {
					boss.y -= 40 * Gdx.graphics.getDeltaTime();

					// Remove boss if it goes off-screen
					if (boss.y + boss.height < 0) {
						hearts=0;
						bossSpawned = false;
					}

					// Check for collision between boss and the player
					if (boss.overlaps(player)) {
						explodingSound.playSound();
						hearts=0;
						bossSpawned = false;
					}

					// Check for collision between boss and player's projectiles
					Iterator<Rectangle> projectileIterator = projectiles.iterator();
					while (projectileIterator.hasNext()) {
						Rectangle projectile = projectileIterator.next();
						if (boss.overlaps(projectile)) {
							explodingSound.playSound();
							projectileIterator.remove();
							bossHealth--;
							if (bossHealth == 0) {
								bossSpawned = false;
								score += 100;
							}
						}
					}
				}

				// Update skill upgrade position
				if (skillUpgradeSpawned) {
					skillUpgrade.y -= 100 * Gdx.graphics.getDeltaTime();

					// Remove skill upgrade if it goes off-screen
					if (skillUpgrade.y + skillUpgrade.height < 0) {
						skillUpgradeSpawned = false;
					}

					// Check for collision between skill upgrade and the player
					if (skillUpgrade.overlaps(player)) {
						skillUpgradeSpawned = false;
						skillUpgradeCollected = true;
						skillUpgradeTime = TimeUtils.nanoTime();
					}
				}
				// Check for collision between aliens and player's projectiles
				Iterator<Rectangle> projectileIterator = projectiles.iterator();
				while (projectileIterator.hasNext()) {
					Rectangle projectile = projectileIterator.next();
					Iterator<Rectangle> alienIterator = aliens.iterator();
					while (alienIterator.hasNext()) {
						Rectangle alien = alienIterator.next();
						if (alien.overlaps(projectile)) {
							explodingSound.playSound();
							projectileIterator.remove();
							alienIterator.remove();
							score += 10;
							spawnExplosion(alien.x, alien.y); // Spawn explosion at alien's position
						}
					}
				}

				// Update alien positions and check for collision with the player
				Iterator<Rectangle> iter = aliens.iterator();
				while (iter.hasNext()) {
					Rectangle alien = iter.next();
					alien.y -= 200 * Gdx.graphics.getDeltaTime();

					if (alien.y + 64 < 0) {
						iter.remove();
					}

					// Check for collision between aliens and the player
					if (alien.overlaps(player)) {
						explodingSound.playSound();
						iter.remove();
						hearts--;
						spawnExplosion(alien.x, alien.y); // Spawn explosion at alien's position
					}

					// Check for collision between aliens and player's projectiles
					projectileIterator = projectiles.iterator();
					while (projectileIterator.hasNext()) {
						Rectangle projectile = projectileIterator.next();
						if (alien.overlaps(projectile)) {
							explodingSound.playSound();
							projectileIterator.remove();
							iter.remove();
							score += 10;
							spawnExplosion(alien.x, alien.y); // Spawn explosion at alien's position
						}
					}


					// Check for collision between aliens and player's projectiles
					projectileIterator = projectiles.iterator();
					while (projectileIterator.hasNext()) {
						Rectangle projectile = projectileIterator.next();
						if (alien.overlaps(projectile)) {
							explodingSound.playSound();
							projectileIterator.remove();
							iter.remove();
							score += 10;
						}
					}
				}

				// Update projectile positions
				if (isProjectileActive) {
					projectileIterator = projectiles.iterator();
					while (projectileIterator.hasNext()) {
						Rectangle projectile = projectileIterator.next();
						projectile.y += 300 * Gdx.graphics.getDeltaTime();
						if (projectile.y > 600) {
							projectileIterator.remove();
							isProjectileActive = false;
						}
					}
				}

				// Update skill upgrade effect
				if (skillUpgradeCollected) {
					if (TimeUtils.nanoTime() - skillUpgradeTime >= SKILL_UPGRADE_DURATION) {
						skillUpgradeCollected = false;
					}
				}

				// Shoot two projectiles if skill upgrade is active
				if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && skillUpgradeCollected) {
					spawnProjectile();
					Rectangle secondProjectile = new Rectangle();
					secondProjectile.y = player.y;
					secondProjectile.x = player.x + 45;
					secondProjectile.setHeight(32);
					secondProjectile.setWidth(32);
					projectiles.add(secondProjectile);
					isProjectileActive = true;
					shootSound.playSound();
					skillUpgradeCollected = false;
					Rectangle thirdProjectile = new Rectangle();
					thirdProjectile.y = player.y;
					thirdProjectile.x = player.x -15;
					thirdProjectile.setHeight(32);
					thirdProjectile.setWidth(32);
					projectiles.add(thirdProjectile);
					isProjectileActive = true;
					shootSound.playSound();
					skillUpgradeCollected = false;
				}

			} else {
				font.draw(batch, "GAME OVER", 350, 325);
				font.draw(batch, "Press ENTER to Restart", 250, 275);
				if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
					restartGame();
				}
				gameOver = true;
			}
		}else {
			font.draw(batch, "GAME OVER", 320, 300);
			font.draw(batch, "Press R to Restart", 300, 250);
			if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
				restartGame();
				if (score > highScore) {
					highScore = score;
				}
			}
		}
		Iterator<Rectangle> explosionIterator = explosions.iterator();
		Iterator<Float> startTimeIterator = explosionStartTimes.iterator();
		while (explosionIterator.hasNext() && startTimeIterator.hasNext()) {
			Rectangle explosion = explosionIterator.next();
			float startTime = startTimeIterator.next();
			batch.draw(explosionsTexture, explosion.x, explosion.y);
			if (isExplosionFinished(startTime)) {
				explosionIterator.remove();
				startTimeIterator.remove();
			}
		}
		batch.end();
	}

	@Override
	public void dispose() {
		batch.dispose();
		playerTexture.dispose();
		enemyTexture.dispose();
		projectileTexture.dispose();
		bossTexture.dispose();
		skillUpgradeTexture.dispose();
		font.dispose();
		explodingSound.dispose();
		shootSound.dispose();
		explosionsTexture.dispose();
	}
}