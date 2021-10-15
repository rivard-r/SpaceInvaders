import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.*;
import tester.*;
import javalib.worldcanvas.*;
import javalib.worldimages.*;
import java.awt.Color;
import javalib.funworld.*; 


// use fold to apply darw method to a list of objects
// Using lambda in java:
// - Can use it when you have an interface that has only one method 
// - 

interface IList<T> {

  // returns the length of this list
  int length();

  // filter this list using the given predicate
  IList<T> filter(Predicate<T> pred);

  // map a function onto every member of this list
  <U> IList<U> map(Function<T, U> converter);

  boolean andMap(Predicate<T> pred);

  boolean orMap(Predicate<T> pred);

  // combine the items in this list from right to left
  <U> U fold(BiFunction<T, U, U> converter, U initial);
}

class MtList<T> implements IList<T> {

  // filter this list using the given predicate
  public IList<T> filter(Predicate<T> pred) {
    return this;
  }

  // map a function onto every member of this list
  public <U> IList<U> map(Function<T, U> converter) {
    return new MtList<U>();
  }

  // combine the items in this list from right to left
  public <U> U fold(BiFunction<T, U, U> converter, U initial) {
    return initial;
  }

  // compute the length of this list
  public int length() {
    return 0;
  }

  public boolean andMap(Predicate<T> pred) {
    return true;
  }

  public boolean orMap(Predicate<T> pred) {
    return false;
  }

}

class ConsList<T> implements IList<T> {
  T first;
  IList<T> rest;

  ConsList(T first, IList<T> rest) {
    this.first = first;
    this.rest = rest;
  }

  // filter this list using the given predicate
  public IList<T> filter(Predicate<T> pred) {
    if (pred.test(this.first)) {
      return new ConsList<T>(this.first, this.rest.filter(pred));
    } else {
      return this.rest.filter(pred);
    }
  }

  // map a function onto every member of this list
  public <U> IList<U> map(Function<T, U> converter) {
    return new ConsList<U>(converter.apply(this.first), this.rest.map(converter));
  }

  // combine the items in this list from right to left
  public <U> U fold(BiFunction<T, U, U> converter, U initial) {
    return converter.apply(this.first, this.rest.fold(converter, initial));
  }

  // compute the length of this list
  public int length() {
    return 1 + this.rest.length();
  }

  public boolean andMap(Predicate<T> pred) {
    return this.map((s)->pred.test(s)).fold(new andMapHelp(), true);
  }

  public boolean orMap(Predicate<T> pred) {
    return this.map((s)->pred.test(s)).fold(new orMapHelp(), false);
  }

}

class andMapHelp implements BiFunction<Boolean, Boolean, Boolean> {
  public Boolean apply(Boolean current, Boolean acc) {
    return current && acc;
  }
}

class orMapHelp implements BiFunction<Boolean, Boolean, Boolean> {
  public Boolean apply(Boolean current, Boolean acc) {
    return current || acc;
  }
}


class CartPt {
  int x;
  int y;

  CartPt(int x, int y) {
    this.x = x;
    this.y = y;
  }
}

interface IGamePiece {

	//move this IGamePiece by the given x and y
	IGamePiece move(int dx, int dy);
	//draw this IGamePiece
	WorldImage draw();
}

abstract class AGamePiece implements IGamePiece {
	CartPt loc;
	Color color;
  int size;

	AGamePiece(CartPt loc, Color color, int size) {
		this.loc = loc;
		this.color = color;
    this.size = size;
	}
}

class Spaceship extends AGamePiece {
  static int SPACESHIP_Y = 10; // change later, trying to set permanent Y
  static int SPACESHIP_SPEED = 20; // change later, arbritrary number
  int speed;

  Spaceship(int xpos, Color color, int size, int speed){
    super(new CartPt(xpos, SPACESHIP_Y), color, size);
    this.speed = SPACESHIP_SPEED;
  }

  public IGamePiece move(int dx, int dy) {
    return new Spaceship(super.loc.x+dx, super.color, super.size, this.speed);
  }

  public WorldImage draw() {
    return new RectangleImage(size*2, size, OutlineMode.OUTLINE, super.color);
  }
}

class Invader extends AGamePiece {

  static Color DEFAULT_COLOR = Color.MAGENTA;
  static int DEFAULT_SIZE = 50; // change later, arbitrary number

  Invader(CartPt loc) {
    super(loc, DEFAULT_COLOR, DEFAULT_SIZE);
  }

  public IGamePiece move(int dx, int dy) {
    return new Invader(new CartPt(super.loc.x+dx, super.loc.y+dy));
  }

  public WorldImage draw() {
    return new RectangleImage(size*2, size, OutlineMode.OUTLINE, super.color);;
  }
}

class InvaderColumn {
  // look at adding an int here to track the stack of invader rows 
  int rank; 
  IList<Invader> invaders;

  InvaderColumn(IList<Invader> invaders, int rank) {
    this.invaders = invaders;
    this.rank = rank; 
  }
}


interface IBullet {
  static int BULLET_SPEED = 5; // change later -----------
  static int BULLET_SIZE = 10; // change later
}

abstract class ABullet implements IBullet{
  CartPt position;
  int size;
  int speed;

  ABullet(CartPt position) {
    this.position = position;
    this.size = BULLET_SIZE;
    this.speed = BULLET_SPEED;
  }
}

class SpaceshipBullet extends ABullet {
  
  SpaceshipBullet(CartPt posn) {
    super(posn);
  }
}

class InvaderBullet extends ABullet {
  
  InvaderBullet(CartPt posn) {
    super(posn);
  }
}

class InvaderBullets {
  IList<InvaderBullet> bullets;

  InvaderBullets(IList<InvaderBullet> bullets) {
    this.bullets = bullets;
  }
}

class SpaceshipBullets {
  IList<SpaceshipBullet> bullets;

  SpaceshipBullets(IList<SpaceshipBullet> bullets) {
    this.bullets = bullets;
  }
}

/*
 * IListDraw<T>()
 *    new WorldScene().placeImageXY(this.first.draw().this.rest.IListDraw())
 *                                 
 *    
 *    where does it draw?
 *  
 *  InvaderColumnmap(draw)
 */






class ExamplesSpaceInvaders {
  ExamplesSpaceInvaders() {
  }
  
  // invader CartPt's
  // Column_Row
  CartPt IP1_1 = new CartPt(100, 800);
  CartPt IP1_2 = new CartPt(100, 700);
  CartPt IP1_3 = new CartPt(100, 600);
  
  CartPt IP2_1 = new CartPt(200, 800);
  CartPt IP2_2 = new CartPt(200, 700);
  CartPt IP2_3 = new CartPt(200, 600);
  
  CartPt IP3_1 = new CartPt(300, 800);
  CartPt IP3_2 = new CartPt(300, 700);
  CartPt IP3_3 = new CartPt(300, 600);
  
  CartPt IP4_1 = new CartPt(400, 800);
  CartPt IP4_2 = new CartPt(400, 700);
  CartPt IP4_3 = new CartPt(400, 600);
  
  CartPt IP5_1 = new CartPt(500, 800);
  CartPt IP5_2 = new CartPt(500, 700);
  CartPt IP5_3 = new CartPt(500, 600);
  
  CartPt IP6_1 = new CartPt(600, 800);
  CartPt IP6_2 = new CartPt(600, 700);
  CartPt IP6_3 = new CartPt(600, 600);
  
  CartPt IP7_1 = new CartPt(700, 800);
  CartPt IP7_2 = new CartPt(700, 700);
  CartPt IP7_3 = new CartPt(700, 600);
  
  CartPt IP8_1 = new CartPt(800, 800);
  CartPt IP8_2 = new CartPt(800, 700);
  CartPt IP8_3 = new CartPt(800, 600);
  
  CartPt IP9_1 = new CartPt(900, 800);
  CartPt IP9_2 = new CartPt(900, 700);
  CartPt IP9_3 = new CartPt(900, 600);
  
  //invaders
  Invader Inv1_1 = new Invader(this.IP1_1);
  Invader Inv1_2 = new Invader(this.IP1_2);
  Invader Inv1_3 = new Invader(this.IP1_3);
  
  Invader Inv2_1 = new Invader(this.IP2_1);
  Invader Inv2_2 = new Invader(this.IP2_2);
  Invader Inv2_3 = new Invader(this.IP2_3);
  
  Invader Inv3_1 = new Invader(this.IP3_1);
  Invader Inv3_2 = new Invader(this.IP3_2);
  Invader Inv3_3 = new Invader(this.IP3_3);
  
  Invader Inv4_1 = new Invader(this.IP4_1);
  Invader Inv4_2 = new Invader(this.IP4_2);
  Invader Inv4_3 = new Invader(this.IP4_3);
  
  Invader Inv5_1 = new Invader(this.IP5_1);
  Invader Inv5_2 = new Invader(this.IP5_2);
  Invader Inv5_3 = new Invader(this.IP5_3);
  
  Invader Inv6_1 = new Invader(this.IP6_1);
  Invader Inv6_2 = new Invader(this.IP6_2);
  Invader Inv6_3 = new Invader(this.IP6_3);
  
  Invader Inv7_1 = new Invader(this.IP7_1);
  Invader Inv7_2 = new Invader(this.IP7_2);
  Invader Inv7_3 = new Invader(this.IP7_3);
  
  Invader Inv8_1 = new Invader(this.IP8_1);
  Invader Inv8_2 = new Invader(this.IP8_2);
  Invader Inv8_3 = new Invader(this.IP8_3);
  
  Invader Inv9_1 = new Invader(this.IP9_1);
  Invader Inv9_2 = new Invader(this.IP9_2);
  Invader Inv9_3 = new Invader(this.IP9_3);
  
  IList<Invader> InvL1 = new ConsList<Invader>(this.Inv1_1, new ConsList<Invader>(this.Inv1_2,
      new ConsList<Invader>(this.Inv1_3, new MtList<Invader>())));
  
  IList<Invader> InvL2 = new ConsList<Invader>(this.Inv2_1, new ConsList<Invader>(this.Inv2_2,
      new ConsList<Invader>(this.Inv2_3, new MtList<Invader>())));
  
  IList<Invader> InvL3 = new ConsList<Invader>(this.Inv3_1, new ConsList<Invader>(this.Inv3_2,
      new ConsList<Invader>(this.Inv3_3, new MtList<Invader>())));
  
  IList<Invader> InvL4 = new ConsList<Invader>(this.Inv4_1, new ConsList<Invader>(this.Inv4_2,
      new ConsList<Invader>(this.Inv4_3, new MtList<Invader>())));
  
  IList<Invader> InvL5 = new ConsList<Invader>(this.Inv5_1, new ConsList<Invader>(this.Inv5_2,
      new ConsList<Invader>(this.Inv5_3, new MtList<Invader>())));
  
  IList<Invader> InvL6 = new ConsList<Invader>(this.Inv6_1, new ConsList<Invader>(this.Inv6_2,
      new ConsList<Invader>(this.Inv6_3, new MtList<Invader>())));

  IList<Invader> InvL7 = new ConsList<Invader>(this.Inv7_1, new ConsList<Invader>(this.Inv7_2,
      new ConsList<Invader>(this.Inv7_3, new MtList<Invader>())));
  
  IList<Invader> InvL8 = new ConsList<Invader>(this.Inv8_1, new ConsList<Invader>(this.Inv8_2,
      new ConsList<Invader>(this.Inv8_3, new MtList<Invader>())));
  
  IList<Invader> InvL9 = new ConsList<Invader>(this.Inv9_1, new ConsList<Invader>(this.Inv9_2,
      new ConsList<Invader>(this.Inv9_3, new MtList<Invader>()))); 
  
  InvaderColumn Col1 = new InvaderColumn(this.InvL1, 1);
  InvaderColumn Col2 = new InvaderColumn(this.InvL2, 2);
  InvaderColumn Col3 = new InvaderColumn(this.InvL3, 3);
  InvaderColumn Col4 = new InvaderColumn(this.InvL4, 4);
  InvaderColumn Col5 = new InvaderColumn(this.InvL5, 5);
  InvaderColumn Col6 = new InvaderColumn(this.InvL6, 6);
  InvaderColumn Col7 = new InvaderColumn(this.InvL7, 7);
  InvaderColumn Col8 = new InvaderColumn(this.InvL8, 8);
  InvaderColumn Col9 = new InvaderColumn(this.InvL9, 9);
  
  IList<InvaderColumn> Columns =
      new ConsList<InvaderColumn>(this.Col1,
          new ConsList<InvaderColumn>(this.Col2,
              new ConsList<InvaderColumn>(this.Col3,
                  new ConsList<InvaderColumn>(this.Col3,
                      new ConsList<InvaderColumn>(this.Col5,
                          new ConsList<InvaderColumn>(this.Col6,
                              new ConsList<InvaderColumn>(this.Col7,
                                  new ConsList<InvaderColumn>(this.Col8,
                                      new ConsList<InvaderColumn>(this.Col9,
                                          new MtList<InvaderColumn>())))))))));
}






