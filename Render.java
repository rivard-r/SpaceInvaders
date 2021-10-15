import tester.*;                
import javalib.worldimages.*;   
import javalib.funworld.*;      
import java.awt.Color;
import java.util.Random; 

/* Needed functions
  -drawShape
*/

class WorldState extends World {
  IList<InvaderColumn> invaders;
  Spaceship spaceship;
  IList<IBullet> bullets;


  public WorldScene makeScene() {
    // fold(convert invaders to list of images) + (spaceship image) + fold(convert bullets to images)
    return invaders.map(new convertInvaderColumnsToImage())
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
