import tester.*;                
import javalib.worldimages.*;   
import javalib.funworld.*;


import java.awt.Color;
import java.util.Random; 

class WorldState extends World {
  IList<IList<Invader>> invaders;
  Spaceship spaceship;
  IList<IBullet> bullets;

  WorldState(IList<IList<Invader>> invaders, Spaceship spaceship, IList<IBullet> bullets) {
    this.invaders = invaders;
    this.spaceship = spaceship;
    this.bullets = bullets;
  }

  public WorldScene makeScene() {
    // fold(convert invaders to list of images) + (spaceship image) + fold(convert bullets to images)
    return bullets.map(new BulletToImage()).fold(new CrunchWorldImage(),
    invaders.map(new InvaderListToImageList()).fold(new CrunchInvaderList(), 
      new WorldScene(1000, 1000)).placeImageXY(spaceship.draw(), 0, 0));
  }

  //move the dots on the scene
	public World onTick() {
		return null;
	}

	//move the dots on the scene
	public World onTickTest() {
		return null;
	}
}
/*
class ExamplesWorld {
  boolean testBigBang(Tester t) {
    WorldState world = new WorldState();
    int worldWidth = 1000;
    int worldHeight = 400;
    double tickRate = 0;
    return world.bigBang(worldWidth, worldHeight, tickRate);
  }
}*/
