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

  @Override
  public IGamePiece move(int dx, int dy) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public WorldImage draw() {
    // TODO Auto-generated method stub
    return null;
  }
}

class Invader extends AGamePiece {

  static Color DEFAULT_COLOR = Color.MAGENTA;
  static int DEFAULT_SIZE = 5; // change later, arbitrary number

  Invader(CartPt loc) {
    super(loc, DEFAULT_COLOR, DEFAULT_SIZE);
  }

  @Override
  public IGamePiece move(int dx, int dy) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public WorldImage draw() {
    // TODO Auto-generated method stub
    return null;
  }
}

class InvaderRow {
  // look at adding an int here to track the stack of invader rows 
  int rank;
  IList<Invader> invaders;

  InvaderRow(IList<Invader> invaders, int rank) {
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







