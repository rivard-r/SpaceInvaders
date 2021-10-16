import tester.*;                
import javalib.worldimages.*;   
import javalib.funworld.*;


import java.awt.Color;
import java.util.Random; 

class WorldState extends World {
  IList<IList<Invader>> invaders;
  Spaceship spaceship;
  IList<IBullet> bullets;

  static RectangleImage board = new RectangleImage(1000, 1000, "outline", Color.black);


  public WorldScene makeScene() {
    // fold(convert invaders to list of images) + (spaceship image) + fold(convert bullets to images)
    return bullets.map(new BulletToImage()).fold(new CrunchWorldImage(), invaders.map(new InvaderListToImageList()).fold(new CrunchInvaderList(), 
      new WorldScene(1000, 1000)).placeImageXY(spaceship.draw(), 0, 0));
  }

  //move the dots on the scene
	public World onTick() {
		ILoDot addDot = new ConsLoDot(new Dot(), this.dots);
		return new Dots(addDot.move());
	}

	//move the dots on the scene
	public World onTickTest() {
		ILoDot addDot = new ConsLoDot(new Dot(new Random(600)), this.dots);
		return new Dots(addDot.move());
	}

}
