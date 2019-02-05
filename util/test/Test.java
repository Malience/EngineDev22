package test;

public class Test {
	public static void nanoCompare(int iterations, Runnable r0, Runnable r1) {
		
		long t0 = 0, d0 = 0, d1 = 0;
		for(int i = 0; i < iterations; i++) {
			t0 = System.nanoTime();
			for(int j = 0; j < iterations; j++) r0.run();
			d0 += System.nanoTime() - t0;
			
			t0 = System.nanoTime();
			for(int j = 0; j < iterations; j++) r1.run();
			d1 += System.nanoTime() - t0;
			
			t0 = System.nanoTime();
			for(int j = 0; j < iterations; j++) r1.run();
			d1 += System.nanoTime() - t0;
			
			t0 = System.nanoTime();
			for(int j = 0; j < iterations; j++) r0.run();
			d0 += System.nanoTime() - t0;
		}
		System.out.println("Delta 0: " + d0);
		System.out.println("Delta 1: " + d1);
	}
	
	public static void milliCompare(int iterations, Runnable r0, Runnable r1) {
		long t0 = 0, d0 = 0, d1 = 0;
		for(int i = 0; i < iterations; i++) {
			t0 = System.currentTimeMillis();
			for(int j = 0; j < iterations; j++) r0.run();
			d0 += System.currentTimeMillis() - t0;
			t0 = System.currentTimeMillis();
			for(int j = 0; j < iterations; j++) r1.run();
			d1 += System.currentTimeMillis() - t0;
			t0 = System.currentTimeMillis();
			for(int j = 0; j < iterations; j++) r1.run();
			d1 += System.currentTimeMillis() - t0;
			t0 = System.currentTimeMillis();
			for(int j = 0; j < iterations; j++) r0.run();
			d0 += System.currentTimeMillis() - t0;
			t0 = System.currentTimeMillis();
			for(int j = 0; j < iterations; j++) r0.run();
			d0 += System.currentTimeMillis() - t0;
			t0 = System.currentTimeMillis();
			for(int j = 0; j < iterations; j++) r1.run();
			d1 += System.currentTimeMillis() - t0;
		}
		System.out.println("Delta 0: " + d0);
		System.out.println("Delta 1: " + d1);
	}
}
