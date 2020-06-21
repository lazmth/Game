package timer;

public class Timer {
	
	// Time value at last measurement taken.
		private double previousTime;
		
		public void init() {
			previousTime = getCurrentTime();
		}
		
		/**
		 * @return Current time, from some arbitrary origin. In seconds.
		 */
		public double getCurrentTime() {
			return System.nanoTime() / 1000_000_000.0;
		}
		
		/**
		 * @return Time passed since last call, in seconds.
		 */
		public float getElapsedTime() {
			double t1 = getCurrentTime();
			float elapsedTime = (float) (t1 - previousTime);
			previousTime = t1;
			return elapsedTime;
		}
		
		public double getPreviousTime() {
			return previousTime;
		}	

}
