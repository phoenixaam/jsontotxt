public class Sample implements Runnable {
    public static void main(String[] args) {
        Sample sample = new Sample();
        Thread thread1 = new Thread(sample, "1");
        Thread thread2 = new Thread(sample, "2");
        thread1.start();
        thread2.start();
    }


    @Override
    public synchronized void run() {
        try {
            wait();
        } catch (InterruptedException e){

    }
        System.out.println(Thread.currentThread().getName());
        notify();
}
}
