import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import tester.*;
import javalib.worldimages.*;
import java.awt.Color;
import javalib.funworld.*;

interface IList<T> {

  // returns the length of this list
  int length();

  // filter this list using the given predicate
  IList<T> filter(Predicate<T> pred);

  // map a function onto every member of this list
  <U> IList<U> map(Function<T, U> converter);

  // check if the predicate is true for all members of the list
  boolean andMap(Predicate<T> pred);

  // check if the predicate is true for at least one member of the list
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

  // check if the predicate is true for all members of the list
  public boolean andMap(Predicate<T> pred) {
    return true;
  }

  // check if the predicate is true for at least one member of the list
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

  // check if the predicate is true for all members of the list
  public boolean andMap(Predicate<T> pred) {
    return this.map((s) -> pred.test(s)).fold(new AndMapHelp(), true);
  }

  // check if the predicate is true for at least one member of the list
  public boolean orMap(Predicate<T> pred) {
    return this.map((s) -> pred.test(s)).fold(new OrMapHelp(), false);
  }
}

// assists andMap by returning true if either passed parameters are true
class AndMapHelp implements BiFunction<Boolean, Boolean, Boolean> {
  public Boolean apply(Boolean current, Boolean acc) {
    return current && acc;
  }
}

// assists orMap by returning true if either passed arguments are true
class OrMapHelp implements BiFunction<Boolean, Boolean, Boolean> {
  public Boolean apply(Boolean current, Boolean acc) {
    return current || acc;
  }
}

// converts an IList<WorldImage> to an IList<Invader> by mapping InvaderToImage 
class InvaderListToImageList implements Function<IList<Invader>, IList<WorldImage>> {

  public IList<WorldImage> apply(IList<Invader> t) {
    return t.map(new InvaderToImage());
  }
}

// converts an invader to a WorldImage by invoking its draw function
class InvaderToImage implements Function<Invader, WorldImage> {

  public WorldImage apply(Invader t) {
    return t.draw();
  }
}

// a fold function which is used to crunch an IList<WorldImage> into a single
//  WorldScene by applying 
// placeImageXY through CrunchWorldImage to each WorldImage
class CrunchInvaderList implements BiFunction<IList<WorldImage>, WorldScene, WorldScene> {

  public WorldScene apply(IList<WorldImage> arg0, WorldScene arg1) {
    return arg0.fold(new CrunchWorldImage(), arg1);
  }
}

// Applies the given WorldImage to the given WorldScene through placImageXY
// Offsets the images by half the board length to the right and down to place the pin in
// the top left of the board
class CrunchWorldImage implements BiFunction<WorldImage, WorldScene, WorldScene> {

  public WorldScene apply(WorldImage arg0, WorldScene arg1) {
    return arg1.placeImageXY(arg0, 300, 300);
  }
}

// converts a bullet to a WorldImage by invoking its draw function
class BulletToImage implements Function<IBullet, WorldImage> {

  public WorldImage apply(IBullet t) {
    return t.draw();
  }
}

// checks if the testing CartPt exists within the IList<CartPt> provided
// during this predicate's construction
class ChordMatch implements Predicate<CartPt> {
  IList<CartPt> points;

  ChordMatch(IList<CartPt> points) {
    this.points = points;
  }

  public boolean test(CartPt t) {
    return points.orMap(new PointsEqual(t));
  }
}

// helps ChordMatch by returning true if the tested CartPt matches the 
// CartPt provided during this predicate's construction in both x and y
class PointsEqual implements Predicate<CartPt> {
  CartPt given;

  PointsEqual(CartPt given) {
    this.given = given;
  }

  public boolean test(CartPt t) {
    return given.x == t.x && given.y == t.y;
  }

}

// Assists MakeListOfColumns by 
class MakeCartPt implements BiFunction<Integer, Integer, CartPt> {

  public CartPt apply(Integer col, Integer row) {
    int baseX = 100;
    int baseY = -500;
    int numCol = 9;
    int numRow = 3;
    return new CartPt(baseX + ((numCol - col) * 50), baseY + ((numRow - row) * 100));
  }
}

//MakeInvader
class MakeInvader implements BiFunction<Integer, Integer, Invader> {

  public Invader apply(Integer col, Integer row) {
    return new Invader(new MakeCartPt().apply(col, row));
  }
}

//make list of invaders
class MakeInvaderList implements Function<Integer, IList<Invader>> {

  public IList<Invader> apply(Integer col) {
    return (new Utils().buildListBi(new MakeInvader(), col, 3));
  }
}

class MakeListOfColumns implements Function<Integer, IList<IList<Invader>>> {

  public IList<IList<Invader>> apply(Integer x) {
    return new Utils().buildList(new MakeInvaderList(), x);
  }
}

// A utility object that contains two generic build list functions 
class Utils {
  // uses a sentinel value a to continue building a list according to the passed function until
  // a reaches 0
  <U> IList<U> buildList(Function<Integer, U> func, int a) {
    if (a > 0) {
      return new ConsList<U>(func.apply(a), this.buildList(func, a - 1));
    } else {
      return new MtList<U>();
    }
  }

  // uses a sentinel value b to continue building a list according to the passed bifunction until
  // b reaches 0, also passes a value a through each iteration 
  <U> IList<U> buildListBi(BiFunction<Integer, Integer, U> func, int a, int b) {
    if (b > 0) {
      return new ConsList<U>(func.apply(a, b), this.buildListBi(func, a, b - 1));
    } else {
      return new MtList<U>();
    }
  }

  public IList<IList<Invader>> makeInvaders(int x) {
    return new MakeListOfColumns().apply(x);
  }
}

// An object representing a Cartesian point (x, y)
class CartPt {
  int x;
  int y;

  CartPt(int x, int y) {
    this.x = x;
    this.y = y;
  }
}

interface IGamePiece {

  // move this IGamePiece by an x and y as dictated by its movement rules and speed
  IGamePiece move();

  // draw this IGamePiece overlaid on a standard 600x600 board
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
  static final int SPACESHIP_Y = -20; // change later, trying to set permanent Y
  static final int SPACESHIP_SPEED = 10; // change later, arbritrary number
  static final Color SPACESHIP_COLOR = Color.BLUE;
  static final int SPACESHIP_SIZE = 30;
  int speed;
  boolean isTravelingRight;

  Spaceship(int xpos, boolean isTravelingRight) {
    super(new CartPt(xpos, SPACESHIP_Y), SPACESHIP_COLOR, SPACESHIP_SIZE);
    this.speed = SPACESHIP_SPEED;
    this.isTravelingRight = isTravelingRight;
  }

  // creates a new spaceship shifted by the number of units dictated by its speed either left or right 
  // according to its current travel path. If the spaceship is at either boarder moving the direciton
  // into the border it will not be changed
  public Spaceship move() {
    if (this.isTravelingRight == true && super.loc.x <= (600-super.size)){
      return new Spaceship(super.loc.x + this.speed, this.isTravelingRight);
    } else if (this.isTravelingRight == false && super.loc.x >= super.size){
      return new Spaceship(super.loc.x - this.speed, this.isTravelingRight);
    } else {
      return this;
    }
  }

  public WorldImage draw() {
    // creates a rectangle outline of 600x600 and moves pinhole to bottom left corner
    WorldImage board = new RectangleImage(600, 600, OutlineMode.OUTLINE, Color.BLACK).movePinhole(-300, 300);
    RectangleImage ship = new RectangleImage(super.size * 2, super.size, OutlineMode.SOLID, super.color);
    // places rectangle image on the board image, offset by the pieces loc from the pinhole
    return new OverlayOffsetImage(board, super.loc.x, super.loc.y, ship);
  }

  // changes the direction of this spaceship to left
  public Spaceship goLeft() {
    return new Spaceship(this.loc.x, false);
  }

  // changes the direction of this spaceship to right
  public Spaceship goRight() {
    return new Spaceship(this.loc.x, true);
  }
}

class Invader extends AGamePiece {

  static final Color INVADER_COLOR = Color.MAGENTA;
  static final int INVADER_SIZE = 15;

  Invader(CartPt loc) {
    super(loc, INVADER_COLOR, INVADER_SIZE);
  }

  // Invaders cannot move
  public Invader move() {
    return this;
  }

  public WorldImage draw() {
    // creates a rectangle outline of 600x600 and moves pinhole to bottom left corner
    WorldImage board = new RectangleImage(600, 600, OutlineMode.OUTLINE, Color.BLACK).movePinhole(-300, 300);
    RectangleImage inv = new RectangleImage(INVADER_SIZE, INVADER_SIZE, OutlineMode.SOLID, super.color);
    // places rectangle image on the board image, offset by the pieces loc from the pinhole
    return new OverlayOffsetImage(board, super.loc.x, super.loc.y, inv);
  }
}

interface IBullet {
   // change later -----------
  static final int BULLET_SIZE = 5; // change later

  // creates a WorldImage to represent the bullet overlaid on the standard 600x600 board
  WorldImage draw();

  // moves the bullet along its trajectory by updating the posn x and y basd on speed
  IBullet updatePosn();
}

abstract class ABullet implements IBullet {
  CartPt position;
  int size;
  int speed;
  Color color;

  ABullet(CartPt position, Color color, int speed) {
    this.position = position;
    this.size = BULLET_SIZE;
    this.speed = speed;
    this.color = color;
  }

  public WorldImage draw() {
    // creates a rectangle outline of 600x600 and moves pinhole to bottom left corner
    WorldImage board = new RectangleImage(600, 600, OutlineMode.OUTLINE, Color.BLACK).movePinhole(-300, 300);
    RectangleImage bul = new RectangleImage(size, size, OutlineMode.SOLID, this.color);
    // places rectangle image on the board image, offset by the pieces loc from the pinhole
    return new OverlayOffsetImage(board, this.position.x, this.position.y, bul);
  }

  public IBullet updatePosn() {
    int oldX = this.position.x;
    int oldY = this.position.y;
    return new SpaceshipBullet(new CartPt(oldX, oldY+this.speed));
  }
}

class SpaceshipBullet extends ABullet {
  static final Color SPACESHIP_BUL_COLOR = Color.GREEN;
  static final int SPACESHIP_BULLET_SPEED = -5; // -y is up in our chord space

  SpaceshipBullet(CartPt posn) {
    super(posn, SPACESHIP_BUL_COLOR, SPACESHIP_BULLET_SPEED);
  }
}

class InvaderBullet extends ABullet {
  static final Color INVADER_BUL_COLOR = Color.ORANGE;
  static final int INVADER_BULLET_SPEED = 5; // +y is down in our chord space

  InvaderBullet(CartPt posn) {
    super(posn, INVADER_BUL_COLOR, INVADER_BULLET_SPEED);
  }
}

class ExamplesSpaceInvaders {

  // bullets examples
  CartPt B1_1 = new CartPt(150, -550);
  CartPt B1_2 = new CartPt(200, -350);
  CartPt B2_1 = new CartPt(450, -100);
  CartPt B2_2 = new CartPt(100, -150);

  IBullet IB1 = new InvaderBullet(B1_1);
  IBullet IB2 = new InvaderBullet(B1_2);
  IBullet SB1 = new SpaceshipBullet(B2_1);
  IBullet SB2 = new SpaceshipBullet(B2_2);

  IList<IBullet> CompleteBullets = new ConsList<IBullet>(IB1,
      new ConsList<IBullet>(IB2, new ConsList<IBullet>(SB1, new ConsList<IBullet>(SB2, new MtList<IBullet>()))));

  // spaceship example
  Spaceship SP1 = new Spaceship(300, true);

  // invader CartPt's
  // Column_Row
  CartPt IP1_1 = new CartPt(100, -500);
  CartPt IP1_2 = new CartPt(100, -400);
  CartPt IP1_3 = new CartPt(100, -300);

  CartPt IP2_1 = new CartPt(150, -500);
  CartPt IP2_2 = new CartPt(150, -400);
  CartPt IP2_3 = new CartPt(150, -300);

  CartPt IP3_1 = new CartPt(200, -500);
  CartPt IP3_2 = new CartPt(200, -400);
  CartPt IP3_3 = new CartPt(200, -300);

  CartPt IP4_1 = new CartPt(250, -500);
  CartPt IP4_2 = new CartPt(250, -400);
  CartPt IP4_3 = new CartPt(250, -300);

  CartPt IP5_1 = new CartPt(300, -500);
  CartPt IP5_2 = new CartPt(300, -400);
  CartPt IP5_3 = new CartPt(300, -300);

  CartPt IP6_1 = new CartPt(350, -500);
  CartPt IP6_2 = new CartPt(350, -400);
  CartPt IP6_3 = new CartPt(350, -300);

  CartPt IP7_1 = new CartPt(400, -500);
  CartPt IP7_2 = new CartPt(400, -400);
  CartPt IP7_3 = new CartPt(400, -300);

  CartPt IP8_1 = new CartPt(450, -500);
  CartPt IP8_2 = new CartPt(450, -400);
  CartPt IP8_3 = new CartPt(450, -300);

  CartPt IP9_1 = new CartPt(500, -500);
  CartPt IP9_2 = new CartPt(500, -400);
  CartPt IP9_3 = new CartPt(500, -300);

  // invaders
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

  IList<CartPt> CPList1 = new ConsList<CartPt>(this.IP1_1,
      new ConsList<CartPt>(this.IP1_2, new ConsList<CartPt>(this.IP1_3, new MtList<CartPt>())));
  IList<CartPt> CPList2 = new ConsList<CartPt>(this.IP3_2,
      new ConsList<CartPt>(this.IP3_3, new ConsList<CartPt>(this.IP3_1, new MtList<CartPt>())));

  IList<Invader> InvL1 = new ConsList<Invader>(this.Inv1_1,
      new ConsList<Invader>(this.Inv1_2, new ConsList<Invader>(this.Inv1_3, new MtList<Invader>())));

  IList<Invader> InvL2 = new ConsList<Invader>(this.Inv2_1,
      new ConsList<Invader>(this.Inv2_2, new ConsList<Invader>(this.Inv2_3, new MtList<Invader>())));

  IList<Invader> InvL3 = new ConsList<Invader>(this.Inv3_1,
      new ConsList<Invader>(this.Inv3_2, new ConsList<Invader>(this.Inv3_3, new MtList<Invader>())));

  IList<Invader> InvL4 = new ConsList<Invader>(this.Inv4_1,
      new ConsList<Invader>(this.Inv4_2, new ConsList<Invader>(this.Inv4_3, new MtList<Invader>())));

  IList<Invader> InvL5 = new ConsList<Invader>(this.Inv5_1,
      new ConsList<Invader>(this.Inv5_2, new ConsList<Invader>(this.Inv5_3, new MtList<Invader>())));

  IList<Invader> InvL6 = new ConsList<Invader>(this.Inv6_1,
      new ConsList<Invader>(this.Inv6_2, new ConsList<Invader>(this.Inv6_3, new MtList<Invader>())));

  IList<Invader> InvL7 = new ConsList<Invader>(this.Inv7_1,
      new ConsList<Invader>(this.Inv7_2, new ConsList<Invader>(this.Inv7_3, new MtList<Invader>())));

  IList<Invader> InvL8 = new ConsList<Invader>(this.Inv8_1,
      new ConsList<Invader>(this.Inv8_2, new ConsList<Invader>(this.Inv8_3, new MtList<Invader>())));

  IList<Invader> InvL9 = new ConsList<Invader>(this.Inv9_1,
      new ConsList<Invader>(this.Inv9_2, new ConsList<Invader>(this.Inv9_3, new MtList<Invader>())));

  IList<IList<Invader>> CompleteInvaders = new ConsList<IList<Invader>>(InvL1,
      new ConsList<IList<Invader>>(InvL2,
          new ConsList<IList<Invader>>(InvL3,
              new ConsList<IList<Invader>>(InvL4,
                  new ConsList<IList<Invader>>(InvL5,
                      new ConsList<IList<Invader>>(InvL6,
                          new ConsList<IList<Invader>>(InvL7, new ConsList<IList<Invader>>(InvL8,
                              new ConsList<IList<Invader>>(InvL9, new MtList<IList<Invader>>())))))))));

  public boolean testDraw(Tester t) {
    WorldImage board = new RectangleImage(600, 600, "outline", Color.black).movePinhole(-300, 300);

    return t.checkExpect(Inv1_1.draw(),
        new OverlayOffsetImage(board, 100, -500, new RectangleImage(15, 15, OutlineMode.SOLID, Color.MAGENTA)));
  }

  // TEST FOR FULL GAME

  boolean testBigBang(Tester t) {
    WorldState world = new WorldState(CompleteInvaders, SP1, CompleteBullets, false);
    int worldWidth = 600;
    int worldHeight = 600;
    double tickRate = 0;
    return world.bigBang(worldWidth, worldHeight, tickRate);
  }

  // test the method ChordMatch and orMap
  public boolean testChordMatch(Tester t) {
    return t.checkExpect(new ChordMatch(this.CPList1).test(new CartPt(800, 900)), false)
        && t.checkExpect(new ChordMatch(this.CPList2).test(new CartPt(200, -400)), true);
  }

  //test the method MakeInvaders and the function classes MakeListOfColumns, 
  // MakeInvaderList, MakeInvader, and MakeCartPt
  public boolean testMakeInvaders(Tester t) {
    return t.checkExpect(new Utils().makeInvaders(9), this.CompleteInvaders)
        && t.checkExpect(new Utils().makeInvaders(1),
            new ConsList<IList<Invader>>(this.InvL9, new MtList<IList<Invader>>()))
        && t.checkExpect(new Utils().makeInvaders(0), new MtList<Invader>());
  }

  // TEST FOR INDIVIDUAL LISTS OF OBJECTS
  /*
   * IList<WorldImage> exampleList = new ConsList<WorldImage>(IB1.draw(), new
   * ConsList<WorldImage>(SP1.draw(), new MtList<WorldImage>())); boolean
   * testBigBang(Tester t) { TestWorldState world = new
   * TestWorldState(exampleList); int worldWidth = 700; int worldHeight = 700;
   * double tickRate = 0; return world.bigBang(worldWidth, worldHeight, tickRate);
   * }
   */

}