package threads;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

// UNIT III - Multithreading.
// The countdown runs in its OWN thread, so the window never freezes while the timer counts.
// Pause / Resume uses wait() and notify() (inter-thread communication).
public class FocusTimer extends Thread {

    private int totalSeconds;
    private int secondsLeft;
    private boolean paused = false;
    private boolean stopped = false;
    private JLabel timeLabel;       // label that shows 24:59, 24:58 ...
    private Runnable onFinish;      // code to run when the time reaches 00:00

    public FocusTimer(int minutes, JLabel timeLabel, Runnable onFinish) {
        super(ThreadHelper.GROUP, "FocusTimerThread");   // put this thread in the group
        this.totalSeconds = minutes * 60;
        this.secondsLeft = totalSeconds;
        this.timeLabel = timeLabel;
        this.onFinish = onFinish;
        setPriority(Thread.NORM_PRIORITY + 1);           // thread priority
    }

    // ----- Methods called by the buttons (they run in the GUI thread) -----

    public synchronized void pauseTimer() {
        paused = true;
    }

    public synchronized void resumeTimer() {
        paused = false;
        notify();                // wake up the timer thread that is waiting
    }

    public synchronized void stopTimer() {
        stopped = true;
        paused = false;
        notify();                // wake up if it was paused, so it can end
    }

    public synchronized int getElapsedMinutes() {
        return (totalSeconds - secondsLeft) / 60;
    }

    // ----- Small synchronized methods: only one thread can use them at a time -----

    private synchronized boolean isStopped() {
        return stopped;
    }

    private synchronized int getSecondsLeft() {
        return secondsLeft;
    }

    private synchronized void tick() {
        secondsLeft--;
    }

    // If paused, the timer thread waits here until resumeTimer() or stopTimer() calls notify()
    private synchronized void waitIfPaused() throws InterruptedException {
        while (paused && !stopped) {
            wait();
        }
    }

    // Show the time on the label. Swing components must be changed from the GUI thread,
    // so we use SwingUtilities.invokeLater.
    private void showTime() {
        int left = getSecondsLeft();
        final String text = String.format("%02d:%02d", left / 60, left % 60);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                timeLabel.setText(text);
            }
        });
    }

    // This method runs in the new thread after start() is called.
    @Override
    public void run() {
        try {
            while (getSecondsLeft() > 0 && !isStopped()) {
                waitIfPaused();
                if (isStopped()) {
                    break;
                }
                showTime();
                Thread.sleep(1000);       // wait one second
                tick();
            }
            if (!isStopped()) {
                showTime();                              // shows 00:00
                SwingUtilities.invokeLater(onFinish);    // tell the panel the session is over
            }
        } catch (InterruptedException e) {
            System.out.println("Timer interrupted");
        } finally {
            System.out.println("Focus timer thread finished");
        }
    }
}
