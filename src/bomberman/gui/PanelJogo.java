package bomberman.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

import sun.security.provider.VerificationProvider;
import bomberman.connection.Connection;
import bomberman.gui.AnimJogador.Instruction;
import bomberman.logic.Bomba.EstadoBomba;
import bomberman.logic.Bomba;
import bomberman.logic.Bomberman;
import bomberman.logic.Jogador;
import bomberman.logic.Jogador.Direcao;
import bomberman.logic.Jogador.EstadoJogador;
import bomberman.logic.Peca;

@SuppressWarnings("serial")
public class PanelJogo extends JPanel implements KeyListener {

	public static final int TILESIZE = 50;
	public static final int DOWN = 0, LEFT = 1, RIGHT = 2, UP = 3, STOP = 0, MOVE = 1;

	private Bomberman bm;
	private Timer timer;
	private double tempo = 0;
	private static final int UPDATERATE = 100;// tempo de refresh objectos
	private static ArrayList<AnimJogador> animacoes = new ArrayList<AnimJogador>();

	private BufferedImage wall, fixedWall, floor, jogador, bomba, explosao, powerup;
	private Image redPlayer, yellowPlayer, bluePlayer, greenPlayer;

	public PanelJogo(Bomberman bm) {
		setFocusable(true);
		this.setMinimumSize(new Dimension(TILESIZE * bm.getMapa().getTamanho(), TILESIZE * bm.getMapa().getTamanho()));
		this.loadImages();
		this.setLayout(new FlowLayout());
		this.setVisible(true);
		this.bm = bm;
		this.addKeyListener(this);
		timer = new Timer(UPDATERATE, timerListener);
		timer.start();

		System.out.println(bm.getJogadores().size());

		for (int i = 0; i < bm.getJogadores().size(); i++) {
			animacoes.add(new AnimJogador(Connection.getInstance().getConnections()[i]));
		}

		// Bomberman.imprimeMapa(bm.getMapa().getTab(),
		// bm.getMapa().getTamanho());
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g); // limpa fundo ...

		BufferedImage img = floor; // default image

		int xi, yi;

		for (int i = 0; i < bm.getMapa().getTamanho(); i++) {
			for (int j = 0; j < bm.getMapa().getTamanho(); j++) {

				if (bm.getMapa().getTab()[i][j] == 'X') {
					img = fixedWall;
				} else if (bm.getMapa().getTab()[i][j] == ' ') {
					img = floor;
				} else if (bm.getMapa().getTab()[i][j] == 'W') {
					img = wall;
				}

				xi = j * TILESIZE;
				yi = i * TILESIZE;

				g.drawImage(img, xi, yi, TILESIZE, TILESIZE, null);
			}
		}

		// impressao bombas
		for (int i = 0; i < bm.getBombas().size(); i++) {
			if (bm.getBombas().get(i).getEstadoBomba() != EstadoBomba.EXPLODINDO) {
				g.drawImage(bomba, (int) (bm.getBombas().get(i).getPos().getX() * TILESIZE),
						(int) (bm.getBombas().get(i).getPos().getY() * TILESIZE), TILESIZE, TILESIZE, null);
			}
		}

		// impressao pwups
		for (int i = 0; i < bm.getPowerUps().size(); i++) {
			// TODO SWITCH CASE PARA VER CONFORME O POWERUP A IMAGEM QUE DEVE
			// SER IMPRESSA

			g.drawImage(powerup, (int) (bm.getPowerUps().get(i).getPos().getX() * TILESIZE),
					(int) (bm.getPowerUps().get(i).getPos().getY() * TILESIZE), TILESIZE, TILESIZE, null);
		}

		int dx1, dx2, dy1, dy2, sx1, sx2, sy1, sy2, dir, move;

		// impressao jogador
		for (int i = 0; i < bm.getJogadores().size(); i++) {

			if (bm.getJogadores().get(i).getEstado() != Peca.Estado.ACTIVO)
				continue;

			if (bm.getJogadores().get(i).getEstadoJogador() == Jogador.EstadoJogador.MOVER)
				move = MOVE;
			else
				move = STOP;

			if (bm.getJogadores().get(i).getUltimaDirecao() == Jogador.Direcao.BAIXO)
				dir = DOWN;
			else if (bm.getJogadores().get(i).getUltimaDirecao() == Jogador.Direcao.CIMA)
				dir = UP;
			else if (bm.getJogadores().get(i).getUltimaDirecao() == Jogador.Direcao.DIREITA)
				dir = RIGHT;
			else
				dir = LEFT;

			dx1 = (int) (bm.getJogadores().get(i).getPos().getX() * TILESIZE);
			dy1 = (int) (bm.getJogadores().get(i).getPos().getY() * TILESIZE);
			dx2 = (int) (bm.getJogadores().get(i).getPos().getX() * TILESIZE) + TILESIZE;
			dy2 = (int) (bm.getJogadores().get(i).getPos().getY() * TILESIZE) + TILESIZE;
			sx1 = (int) move * redPlayer.getWidth(null) / 4;
			sy1 = (int) dir * redPlayer.getHeight(null) / 4;
			sx2 = (int) sx1 + redPlayer.getWidth(null) / 4;
			sy2 = (int) sy1 + redPlayer.getHeight(null) / 4;

			g.drawImage(redPlayer, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);

			// g.drawImage(jogador, (int)
			// (bm.getJogadores().get(i).getPos().getX() * TILESIZE),
			// (int) (bm.getJogadores().get(i).getPos().getY() * TILESIZE),
			// TILESIZE, TILESIZE, null);
		}

		// impressao explosao
		for (int i = 0; i < bm.getBombas().size(); i++) {

			if (bm.getBombas().get(i).getEstado() == Peca.Estado.INATIVO)
				continue;

			if (bm.getBombas().get(i).getEstadoBomba() == EstadoBomba.EXPLODINDO) {

				int ignore[] = { 0, 0, 0, 0 };
				int x = (int) bm.getBombas().get(i).getPos().getX();
				int y = (int) bm.getBombas().get(i).getPos().getY();

				g.drawImage(explosao, x * TILESIZE, y * TILESIZE, TILESIZE, TILESIZE, null);

				// impressao explosao
				for (int j = 1; j <= bm.getBombas().get(i).getRaio(); j++) {
					// y++
					if (y + j < bm.getMapa().getTamanho() - 1 && bm.getMapa().getTab()[y + j][x] != 'X' && ignore[0] != 1) {
						if (bm.getMapa().getTab()[y + j][x] == 'W') {
							ignore[0] = 1;
							g.drawImage(explosao, x * TILESIZE, (y + j) * TILESIZE, TILESIZE, TILESIZE, null);
						}

						if (ignore[0] != 1)
							g.drawImage(explosao, x * TILESIZE, (y + j) * TILESIZE, TILESIZE, TILESIZE, null);
					} else
						ignore[0] = 1;
					// y--
					if (y - j > 0 && bm.getMapa().getTab()[y - j][x] != 'X' && ignore[1] != 1) {
						if (bm.getMapa().getTab()[y - j][x] == 'W') {
							ignore[1] = 1;
							g.drawImage(explosao, x * TILESIZE, (y - j) * TILESIZE, TILESIZE, TILESIZE, null);
						}

						if (ignore[1] != 1)
							g.drawImage(explosao, x * TILESIZE, (y - j) * TILESIZE, TILESIZE, TILESIZE, null);
					} else
						ignore[1] = 1;
					// x++
					if (x + j < bm.getMapa().getTamanho() - 1 && bm.getMapa().getTab()[y][x + j] != 'X' && ignore[2] != 1) {
						if (bm.getMapa().getTab()[y][x + j] == 'W') {
							ignore[2] = 1;
							g.drawImage(explosao, (x + j) * TILESIZE, y * TILESIZE, TILESIZE, TILESIZE, null);
						}

						if (ignore[2] != 1)
							g.drawImage(explosao, (x + j) * TILESIZE, y * TILESIZE, TILESIZE, TILESIZE, null);
					} else
						ignore[2] = 1;
					// x--
					if (x - j > 0 && bm.getMapa().getTab()[y][x - j] != 'X' && ignore[3] != 1) {
						if (bm.getMapa().getTab()[y][x - j] == 'W') {
							ignore[3] = 1;
							g.drawImage(explosao, (x - j) * TILESIZE, y * TILESIZE, TILESIZE, TILESIZE, null);
						}

						if (ignore[3] != 1)
							g.drawImage(explosao, (x - j) * TILESIZE, y * TILESIZE, TILESIZE, TILESIZE, null);
					} else
						ignore[3] = 1;
				}
			}
		}
	}

	public void loadImages() {
		try {
			wall = ImageIO.read(new File(System.getProperty("user.dir") + "\\resources\\Wall.png"));
			fixedWall = ImageIO.read(new File(System.getProperty("user.dir") + "\\resources\\FixedWall.png"));
			floor = ImageIO.read(new File(System.getProperty("user.dir") + "\\resources\\Floor.png"));
			jogador = ImageIO.read(new File(System.getProperty("user.dir") + "\\resources\\Jogador.png"));
			bomba = ImageIO.read(new File(System.getProperty("user.dir") + "\\resources\\Bomba.png"));
			explosao = ImageIO.read(new File(System.getProperty("user.dir") + "\\resources\\explosao.png"));
			powerup = ImageIO.read(new File(System.getProperty("user.dir") + "\\resources\\powerup.png"));
			redPlayer = ImageIO.read(new File(System.getProperty("user.dir") + "\\resources\\playerRed.png"));
			yellowPlayer = ImageIO.read(new File(System.getProperty("user.dir") + "\\resources\\playerAmarelo.png"));
			greenPlayer = ImageIO.read(new File(System.getProperty("user.dir") + "\\resources\\playerVerde.png"));
			bluePlayer = ImageIO.read(new File(System.getProperty("user.dir") + "\\resources\\playerAzul.png"));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {

		for (Iterator<Jogador> it = bm.getJogadores().iterator(); it.hasNext();) {

			Jogador j = it.next();

			if (j.getEstado() != Peca.Estado.ACTIVO)
				continue;

			j.setEstadoJogador(EstadoJogador.MOVER);
			if (j.getId() == 1) {

				if (e.getKeyCode() == KeyEvent.VK_UP) {
					j.move(Direcao.CIMA, bm.getMapa());
				} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					j.move(Direcao.BAIXO, bm.getMapa());
				} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
					j.move(Direcao.DIREITA, bm.getMapa());
				} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
					j.move(Direcao.ESQUERDA, bm.getMapa());
				} else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					bm.colocarBomba(j);
				}

				bm.checkPowerUp(j);
			}

			else {
				if (e.getKeyCode() == KeyEvent.VK_W) {
					j.move(Direcao.CIMA, bm.getMapa());
				} else if (e.getKeyCode() == KeyEvent.VK_S) {
					j.move(Direcao.BAIXO, bm.getMapa());
				} else if (e.getKeyCode() == KeyEvent.VK_D) {
					j.move(Direcao.DIREITA, bm.getMapa());
				} else if (e.getKeyCode() == KeyEvent.VK_A) {
					j.move(Direcao.ESQUERDA, bm.getMapa());
				} else if (e.getKeyCode() == KeyEvent.VK_Z) {
					bm.colocarBomba(j);
				}

				bm.checkPowerUp(j);
			}
		}

		this.repaint();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		bm.getJogadores().get(0).setEstadoJogador(EstadoJogador.PARADO);

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	ActionListener timerListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			tempo += UPDATERATE;

			for (int i = 0; i < animacoes.size(); i++) {
				if (animacoes.get(i).getNextInstruction() == Instruction.MOVE) {
					bm.moveJogador(bm.getJogadores().get(i), animacoes.get(i).getDir());
				}
				if (animacoes.get(i).getNextInstruction() == Instruction.PLANTBOMB) {
					bm.colocarBomba(bm.getJogadores().get(i));
				}
			}

			bm.updateBomba(UPDATERATE);
			bm.verificaJogador(UPDATERATE);

			repaint();
		}
	};
}
