package general;


import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;


import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;

import org.junit.Test;

import utils.Locks;

public class LockingTest {
	public static long HOUR = 3600000;

	static class Flag {
		boolean flag;
		public Flag( boolean flag ) {
			this.flag = flag;
		}
		public boolean is() {
			return flag;
		}
		public void set( boolean flag ) {
			this.flag = flag;
		}
	}
	@Test
	public void testLocks() {
		running( fakeApplication(), new Runnable() {
			@Override
			public void run() {
				final Flag gotLock = new Flag( false );
				
				try {
					Locks l = Locks.create()
							.read( "Obj #1")
							.read( "Obj #2")
							.write("Obj #3")
							.acquire();
					assertThat(l.acquired!=null );

					Locks l2 = Locks.create()
							.read( "Obj #1")
							.read( "Obj #2")
							.acquire();

					assertThat(l2.acquired!=null );

					Thread lockMe = new Thread( new Runnable() {
						public void run() {
							try {
							Locks l = Locks.create()
									.read( "Obj #1")
									.write( "Obj #2")
									.acquire();
							// now we are locked for a bit
							gotLock.set( true );
							} catch( Exception e ) {}
							finally {
								l.release();
							}
						}
					});
					
					assertThat( lockMe.getState() != Thread.State.RUNNABLE );
					l.release();
					assertThat( lockMe.getState() != Thread.State.RUNNABLE );
					l2.release();
					try {
						lockMe.join( 1000 );
						assertThat( gotLock.is() );
					} catch( Exception e ) {
						fail( "Lock not acquired ");
					}
					
				} catch( Exception e) {
					// nothing
				}
			}
		} );
	}
}
