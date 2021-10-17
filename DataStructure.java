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

  public boolean andMap(Predicate<T> pred) {
    return this.map((s) -> pred.test(s)).fold(new andMapHelp(), true);
  }

  public boolean orMap(Predicate<T> pred) {
    return this.map((s) -> pred.test(s)).fold(new orMapHelp(), false);
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

class InvaderListToImageList implements Function<IList<Invader>, IList<WorldImage>> {

  public IList<WorldImage> apply(IList<Invader> t) {
    return t.map(new InvaderToImage());
  }
}

class InvaderToImage implements Function<Invader, WorldImage> {

  public WorldImage apply(Invader t) {
    return t.draw();
  }
}

class CrunchInvaderList implements BiFunction<IList<WorldImage>, WorldScene, WorldScene> {

  public WorldScene apply(IList<WorldImage> arg0, WorldScene arg1) {
    return arg0.fold(new CrunchWorldImage(), arg1);
  }

}

class CrunchWorldImage implements BiFunction<WorldImage, WorldScene, WorldScene> {

  public WorldScene apply(WorldImage arg0, WorldScene arg1) {
    return arg1.placeImageXY(arg0, 300, 300);
  }

}

class BulletToImage implements Function<IBullet, WorldImage> {

  public WorldImage apply(IBullet t) {
    return t.draw();
  }
}

class ChordMatch implements Predicate<CartPt> {
  IList<CartPt> points;

  ChordMatch(IList<CartPt> points) {
    this.points = points;
  }

  public boolean test(CartPt t) {
    return points.orMap(new PointsEqual(t));
  }
}

class PointsEqual implements Predicate<CartPt> {
  CartPt given;

  PointsEqual(CartPt given) {
    this.given = given;
  }

  public boolean test(CartPt t) {
    return given.x == t.x && given.y == t.y;
  }

}

//MakeCartPt
class MakeCartPt implements BiFunction<Integer, Integer, CartPt> {

  public CartPt apply(Integer col, Integer row) {
    int BASE_X = 0;
    int BASE_Y = -550;
    return new CartPt(BASE_X + (col * 50), BASE_Y + (row * 50));
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

  @Override
  public IList<IList<Invader>> apply(Integer x) {

    return new Utils().buildList(new MakeInvaderList(), x);
  }
}

class Utils {
  <U> IList<U> buildList(Function<Integer, U> func, int a) {
    if (a > 0) {
      return new ConsList<U>(func.apply(a), this.buildList(func, a - 1));
    } else {
      return new MtList<U>();
    }
  }

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

class CartPt {
  int x;
  int y;

  CartPt(int x, int y) {
    this.x = x;
    this.y = y;
  }
}

interface IGamePiece {

  // move this IGamePiece by the given x and y
  IGamePiece move(int dx, int dy);

  // draw this IGamePiece
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
  static int SPACESHIP_Y = -20; // change later, trying to set permanent Y
  static int SPACESHIP_SPEED = 20; // change later, arbritrary number
  static Color SPACESHIP_COLOR = Color.BLUE;
  int speed;

  Spaceship(int xpos, Color color, int size, int speed) {
    super(new CartPt(xpos, SPACESHIP_Y), color, size);
    this.speed = SPACESHIP_SPEED;
  }

  public IGamePiece move(int dx, int dy) {
    return new Spaceship(super.loc.x + dx, super.color, super.size, this.speed);
  }

  public WorldImage draw() {
    // creates a rectangle outline of 1000x1000 and moves pinhole to bottom left
    // corner
    WorldImage board = new RectangleImage(600, 600, OutlineMode.OUTLINE, Color.BLACK).movePinhole(-300, 300);
    WorldImage testBoard = new VisiblePinholeImage(board, Color.RED);
    RectangleImage ship = new RectangleImage(super.size * 2, super.size, OutlineMode.SOLID, super.color);
    // places rectangle image on the board image, offset by the pieces loc from the
    // pinhole
    return new OverlayOffsetImage(testBoard, super.loc.x, super.loc.y, ship);
  }
}

class Invader extends AGamePiece {

  static final Color DEFAULT_COLOR = Color.MAGENTA;
  static final int DEFAULT_SIZE = 15; // change later, arbitrary number

  Invader(CartPt loc) {
    super(loc, DEFAULT_COLOR, DEFAULT_SIZE);
  }

  public IGamePiece move(int dx, int dy) {
    return new Invader(new CartPt(super.loc.x + dx, super.loc.y + dy));
  }

  public WorldImage draw() {
    // creates a rectangle outline of 1000x1000 and moves pinhole to bottom left
    // corner
    WorldImage board = new RectangleImage(600, 600, OutlineMode.OUTLINE, Color.BLACK).movePinhole(-300, 300);
    WorldImage testBoard = new VisiblePinholeImage(board, Color.RED);
    RectangleImage inv = new RectangleImage(DEFAULT_SIZE, DEFAULT_SIZE, OutlineMode.SOLID, super.color);
    // places rectangle image on the board image, offset by the pieces loc from the
    // pinhole
    return new OverlayOffsetImage(testBoard, super.loc.x, super.loc.y, inv);
  }
}

// Stop Using
class InvaderColumn {
  // look at adding an int here to track the stack of invader rows
  int column;
  IList<Invader> invaders;

  InvaderColumn(IList<Invader> invaders, int rank) {
    this.invaders = invaders;
    this.column = rank;
  }
}

interface IBullet {
  static int BULLET_SPEED = 5; // change later -----------
  static int BULLET_SIZE = 5; // change later

  WorldImage draw();
}

abstract class ABullet implements IBullet {
  CartPt position;
  int size;
  int speed;

  ABullet(CartPt position) {
    this.position = position;
    this.size = BULLET_SIZE;
    this.speed = BULLET_SPEED;
  }

  public WorldImage draw() {
    // creates a rectangle outline of 1000x1000 and moves pinhole to bottom left
    // corner
    WorldImage board = new RectangleImage(600, 600, OutlineMode.OUTLINE, Color.BLACK).movePinhole(-300, 300);
    RectangleImage bul = new RectangleImage(size, size, OutlineMode.SOLID, Color.RED);
    // places rectangle image on the board image, offset by the pieces loc from the
    // pinhole
    return new OverlayOffsetImage(board, this.position.x, this.position.y, bul);
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
 * IListDraw<T>() new
 * WorldScene().placeImageXY(this.first.draw().this.rest.IListDraw())
 * 
 * 
 * where does it draw?
 * 
 * InvaderColumnmap(draw)
 */

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
  Spaceship SP1 = new Spaceship(300, Color.RED, 15, 10);

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
    WorldImage board = new RectangleImage(1000, 1000, "outline", Color.black).movePinhole(-1000, -1000);

    return t.checkExpect(Inv1_1.draw(),
        new OverlayOffsetImage(board, 100, 800, new RectangleImage(50, 50, OutlineMode.SOLID, Color.MAGENTA)));
  }

  // TEST FOR FULL GAME

  boolean testBigBang(Tester t) {
    WorldState world = new WorldState(CompleteInvaders, SP1, CompleteBullets);
    int worldWidth = 600;
    int worldHeight = 600;
    double tickRate = 0;
    return world.bigBang(worldWidth, worldHeight, tickRate);
  }
  /*
   * IList<WorldImage> exampleList = new ConsList<WorldImage>(IB1.draw(), new
   * ConsList<WorldImage>(SP1.draw(), new MtList<WorldImage>())); boolean
   * testBigBang(Tester t) { TestWorldState world = new
   * TestWorldState(exampleList); int worldWidth = 700; int worldHeight = 700;
   * double tickRate = 0; return world.bigBang(worldWidth, worldHeight, tickRate);
   * }
   */

}