import tester.*;                
import javalib.worldimages.*;   
import javalib.funworld.*;      
import java.awt.Color;
import java.util.Random;     

class Dots extends World {
	ILoDot dots;
	Dots(ILoDot dots) {
		this.dots = dots;
	}
	//draws the dots onto the background
	public WorldScene makeScene() {
		return this.dots.draw(new WorldScene(600, 400));
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



interface ILoDot {
	//draw the dots in this ILoDot onto the scene
	WorldScene draw(WorldScene acc);
	//move the dots in this ILoDot
	ILoDot move();
}

class MtLoDot implements ILoDot {

	//draws dots from this list onto the accumulated
	//image of the scene so far
	public WorldScene draw(WorldScene acc) {
		return acc;
	}

	//move the dots in this empty list
	public ILoDot move() {
		return this;
	}
}

class ConsLoDot implements ILoDot {
	Dot first;
	ILoDot rest;
	ConsLoDot(Dot first, ILoDot rest) {
		this.first = first;
		this.rest = rest;
	}
	//draws dots from this list onto the accumulated
	//image of the scene so far
	public WorldScene draw(WorldScene acc) {
		return this.rest.draw(this.first.draw(acc));
	}

	//move the dots in this non-empty list
	public ILoDot move() {
		return new ConsLoDot(this.first.move(), this.rest.move());
	}

}

class Dot {
	int radius;
	Color c;
	int x;
	int y;
	Random rand;

	Dot(int radius, Color c, int x, int y, Random rand) {
		this.radius = radius;
		this.c = c;
		this.x = x;
		this.y = y;
		this.rand = rand;
	}

	Dot() {
		this(10, Color.magenta, 0,0, new Random());
		this.x = rand.nextInt(600);
		this.y = rand.nextInt(400);
	}

	//constructor for testing
	Dot(Random rand) {
		this(10, Color.magenta, 0, 0, rand); //new Random(1000));
		this.x = rand.nextInt(600);
		this.y = rand.nextInt(400);
	}

	//draw this dot
	WorldScene draw(WorldScene acc) {
		return acc.placeImageXY(new CircleImage(this.radius, "solid", this.c), this.x, this.y);
	}
	
	//create a new dot that is shifted on the x-axis and the y-axis
	Dot move() {
		return new Dot(this.radius, this.c, this.x + 5, this.y + 3, this.rand);
	}
}

class Examples {
	Dot d1 = new Dot(new Random(1000));
	Dot d2 = new Dot(new Random(1000));
	Dot d3 = new Dot();
	ILoDot mt = new MtLoDot();
	ILoDot lod1 = new ConsLoDot(this.d1, this.mt);
	ILoDot lod2 = new ConsLoDot(this.d2, this.lod1);
	ILoDot lod3 = new ConsLoDot(this.d3, this.lod2);
	Dots world0 = new Dots(this.lod2);

	boolean testMove(Tester t) {
		return t.checkExpect(this.d1.move(), 
				new Dot(this.d1.radius, this.d1.c, this.d1.x + 5, this.d1.y + 3, this.d1.rand)) &&
				t.checkExpect(this.lod2.move(), 
						new ConsLoDot(new Dot(this.d2.radius, this.d2.c, this.d2.x + 5, this.d2.y + 3, this.d1.rand), 
								new ConsLoDot(new Dot(this.d1.radius, this.d1.c, this.d1.x + 5, this.d1.y + 3, this.d1.rand),
										this.mt))) &&
				t.checkExpect(this.world0.onTickTest(), new Dots(new ConsLoDot(new Dot(10, Color.magenta, 77,179, this.d1.rand),
						new ConsLoDot(new Dot(this.d2.radius, this.d2.c, this.d2.x + 5, this.d2.y + 3, this.d1.rand), 
								new ConsLoDot(new Dot(this.d1.radius, this.d1.c, this.d1.x + 5, this.d1.y + 3, this.d1.rand),
										this.mt)))));
	}

	boolean testDraw(Tester t) {
		return t.checkExpect(this.d1.draw(new WorldScene(600,400)),
				new WorldScene(600,400).placeImageXY(new CircleImage(10, "solid", Color.magenta), 487, 135)) &&
				t.checkExpect(this.lod2.draw(new WorldScene(600, 400)),
						new WorldScene(600,400).placeImageXY(new CircleImage(10, "solid", Color.magenta), 487, 135)
				.placeImageXY(new CircleImage(10, "solid", Color.magenta), 487, 135));
				
	}
	
	boolean testBigBang(Tester t) {
		Dots world = new Dots(this.mt);
		int worldWidth = 600;
		int worldHeight = 400;
		double tickRate = .02;
		return world.bigBang(worldWidth, worldHeight, tickRate);
	}

}