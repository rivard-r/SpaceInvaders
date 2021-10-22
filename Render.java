import javalib.worldimages.*;

import java.awt.Color;

import javalib.funworld.*;

class TestWorldState extends World {
  IList<WorldImage> list;

  TestWorldState(IList<WorldImage> list) {
    this.list = list;
  }

  public WorldScene makeScene() {
    // fold(convert invaders to list of images) + (spaceship image) + fold(convert
    // bullets to images)
    return list.fold(new CrunchWorldImage(), new WorldScene(600, 600));
  }

  // not yet implemented
  public World onTick() {
    return null;
  }

  // not yet implemented
  public World onTickTest() {
    return null;
  }
}

// Represents the current world and contains all data relevent to generate the world
class WorldState extends World {
  IList<IList<Invader>> invaders;
  Spaceship spaceship;
  IList<IBullet> bullets;
  int whoWon; //added this for easy endgame check

  WorldState(IList<IList<Invader>> invaders, Spaceship spaceship, IList<IBullet> bullets, int whoWon) {
    this.invaders = invaders; //list of list of IGamePiece
    this.spaceship = spaceship;
    this.bullets = bullets; // list of IGamePiece
    this.whoWon = whoWon; // added this for easy endgame check
  }
  
  // process any game rules involving overlaps and invader generation assumes
  // movement for this tick has already resolved  
  public WorldState resolveEvents() {
    IList<IList<Invader>> invList = invaders;
    return new WorldState(
        this.invaders.map(new FilterInvColumn(this.bullets)),
        this.spaceship,
        this.bullets.filter(new BulInContact(invList)),
        this.winner());

  }

  // produces a new world following on tick game rule behavior of motion for all IGamePieces, 
  // ignores all collision and game ending events
  public WorldState updateWorld() {
    return new WorldState(
      this.invaders,
      this.spaceship.move(), 
      randomFire(this.bullets).map(s->s.updatePosn()).filter(s->s.inBounds()),
      this.whoWon);
  }

  // determines if the game has been won or lost. checks if all invader columns are empty; if yes,
  // user won. otherwise, checks if the spaceship has been hit. if yes, invaders won. otherwise
  // returns 0.
  // 1 = Spaceship won, -1 = invaders won, 0 = ongoing
  public int winner() {
    if (this.invaders.andMap((invCol) -> (invCol.length() == 0))) {
      return 1;
    }
    else if (this.bullets.orMap((b) -> b.checkHit(this.spaceship))) {
      return -1;
    }
    else return 0;
  }


  // generates a random number of shots to fire on this tick cycle that is within the 10 shot
  // maximum and passes this number to randomFireHelper
  public IList<IBullet> randomFire(IList<IBullet> bullets) {
    // max allowed bullets at any given time is 10
    // shotsAvailible is found as max allowed - the sum of all invader bullets in this list of 
    // bullets. Found through tallying invader bullets with sumInvader and summing the total with
    // fold
    int shotsAvailible = 10 - this.bullets.map(s->s.sumInvader()).fold((s1,s2)->s1+s2, 0);
    System.out.println(shotsAvailible);
    // builds a new list of bullets with a random chance of newly fired bullets attached to the old list
    return buildFiredBullets(bullets, invaders.map(new MayFireList(shotsAvailible, this.invaders.length(), bullets)).fold(new FlattenCartPtList(), new MtList<CartPt>()).filter(s->s.x != 999));
  }

  // converts an IList<CartPt> to an IList<IBullet> using the InvaderBullet constructior
  private IList<IBullet> buildFiredBullets(IList<IBullet> bullets, IList<CartPt> newBulletsLocations) {
    return bullets.fold(new AppendBulletLists(), (newBulletsLocations.map(s->new InvaderBullet(s))));
  }

  // handle player input of space, left, and right arrow keys
  public World onKeyEvent(String key) {
    if (key.equals(" ")){
      return new WorldState(this.invaders, this.spaceship, spaceshipFire(), this.whoWon);
    } else if (key.equals("left")) {
      return new WorldState(this.invaders, this.spaceship.goLeft(), this.bullets, this.whoWon); 
    } else if (key.equals("right")) {
      return new WorldState(this.invaders, this.spaceship.goRight(), this.bullets, this.whoWon);
    } else {
      return this;
    }
  }
  
  // returns a new SpaceshipBullet at the top center of the this spaceship's outline
  private IList<IBullet> spaceshipFire() {
    if (this.bullets.length() - this.bullets.map(s->s.sumInvader()).fold((s1,s2)->s1+s2, 0) < 3) {
      return spaceshipFireHelper();
    } else {
      return this.bullets;
    }
  }
  // returns a new SpaceshipBullet at the top center of the this spaceship's outline
  private IList<IBullet> spaceshipFireHelper() {
    int shipX = this.spaceship.loc.x;
    int shipY = this.spaceship.loc.y - this.spaceship.size/2;
    return new ConsList<IBullet>(new SpaceshipBullet(new CartPt(shipX, shipY)), this.bullets);
  }

  public WorldScene makeScene() {
    // fold(convert invaders to list of images) + (spaceship image) +
    // fold(convert bullets to images)
    // generates a graphic using the data contained in WorldState
    // following this process:
    // 1.) Convert IList<IList<Invaders>> to IList<IList<WorldImage>>
    // using Map of InvaderListToImageList
    // 2.) Fold the IList<IList<WorldImage>> generated by 1. into a
    // single WorldScene using placeImageXY
    // 3.) attach the spaceship to the WorldScene generated by 2.
    // using placeImageXY
    // 4.) Convert IList<Bullet> to IList<WorldImage> using Map
    // of BulletToImage
    // 5.) Fold the IList<WorldImage> generated by 4.
    // using placeImageXY with 3. ast the initial condition
    return bullets.map(new BulletToImage()).fold(new CrunchWorldImage(),
        invaders.map(new InvaderListToImageList())
            .fold(new CrunchInvaderList(), new WorldScene(600, 600))
            .placeImageXY(spaceship.draw(), 300, 300));
  }

  // every tick run through the movment game rules and bullet generation using 
  // updateWorld() then check for any bullet-gamePiece overlap with resolveEvents()
  // and modify the world accordingly, also end the game if whoWon has been set from 0
  public World onTick() {
    if (this.whoWon == -1) {
      this.endOfWorld("Game Over");
      return this;
    } else if (this.whoWon == 1) {
      this.endOfWorld("You Won");
      return this;
    } else {
      // add .resolveEvents() here when ready
    return this.updateWorld().resolveEvents();
    }
  }

  // required by this.endOfWorld
  public WorldScene lastScene(String msg) {
    return this.makeScene().placeImageXY(new TextImage(msg, Color.BLACK), 0, 0);
  }
}