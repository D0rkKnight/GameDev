package Entities;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex2f;

import Collision.Collider;
import GameController.Input;
import Shaders.Shader;
import Wrappers.Position;
import Wrappers.Sprites;
import Wrappers.Stats;

public class Player extends Combatant{
	
	public Input input;
	
	public Player(int ID, Position position, Sprites sprites, Shader shader, Stats stats) {
		super(ID, position, sprites, shader, stats);
		input = new Input();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void hit(Collider collider1, Collider collider2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void attack() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void die() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void calculate() {
		// TODO Auto-generated method stub
		move();
	}

	@Override
	public void setFrame(int framenum) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void move() {
		// TODO Auto-generated method stub
		position.x += input.moveX * 0.01;
		position.y += input.moveY * 0.01;
	}

	@Override
	public void getPosition() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render() {
		float dim = 0.2f;
		
		// TODO Auto-generated method stub
		shader.bind();
		glBegin(GL_QUADS);
			glVertex2f(position.x, position.y);
			glVertex2f(position.x + dim, position.y);
			glVertex2f(position.x + dim, position.y + dim);
			glVertex2f(position.x, position.y + dim);
		glEnd();
	}

}
