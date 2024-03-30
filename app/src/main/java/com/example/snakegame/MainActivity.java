package com.example.snakegame;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private final List<SnakePoints> snakePointsList = new ArrayList<>();
    private SurfaceView surfaceView;
    private TextView scoreTV;

    private SurfaceHolder surfaceHolder;

    private String movingPosition = "right";
    private int score = 0;

    private static final int pointSize = 28;
    private static final int defaultTailPoints = 3;
    private static final int snakeColor = Color.YELLOW;
    private static final int snakeMovingSpeed = 800;

    private int positionX, positionY;

    private Canvas canvas = null;
    private Paint pointColor = null;

    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = findViewById(R.id.surfaceView);
        scoreTV = findViewById(R.id.scoreTV);

        final AppCompatImageButton topBtn = findViewById(R.id.topBtn);
        final AppCompatImageButton leftBtn = findViewById(R.id.leftBtn);
        final AppCompatImageButton rightBtn = findViewById(R.id.rightBtn);
        final AppCompatImageButton bottomBtn = findViewById(R.id.bottomBtn);

        surfaceView.getHolder().addCallback(this);
        topBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                movingPosition = "top";
            }
        });

        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                movingPosition = "left";
            }
        });

        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                movingPosition = "right";
            }
        });

        bottomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                movingPosition = "bottom";
            }
        });

    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        this.surfaceHolder = holder;
        init();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    private void init() {
        snakePointsList.clear();
        scoreTV.setText("0");
        score = 0;
        movingPosition = "right";
        int startPositionX = (pointSize) * defaultTailPoints;

        for (int i = 0; i < defaultTailPoints; i++) {
            SnakePoints snakePoints = new SnakePoints(startPositionX, pointSize);
            snakePointsList.add(snakePoints);
            startPositionX = startPositionX - (pointSize * 2);
        }

        addPoint(); // Yemi ekle

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                moveSnake();
                handler.postDelayed(this, snakeMovingSpeed);
            }
        };
        handler.postDelayed(runnable, snakeMovingSpeed);
    }

    private void addPoint() {
        int surfaceWidth = surfaceView.getWidth() - (pointSize * 2);
        int surfaceHeight = surfaceView.getHeight() - (pointSize * 2);

        int randomXPosition = new Random().nextInt(surfaceWidth / pointSize);
        int randomYPosition = new Random().nextInt(surfaceHeight / pointSize);

        if ((randomXPosition % 2) != 0) {
            randomXPosition = randomXPosition + 1;
        }
        if ((randomYPosition % 2) != 0) {
            randomYPosition = randomYPosition + 1;
        }

        positionX = (pointSize * randomXPosition) + pointSize;
        positionY = (pointSize * randomYPosition) + pointSize;
    }

    private void moveSnake() {
        int headPositionX = snakePointsList.get(0).getPositionX();
        int headPositionY = snakePointsList.get(0).getPositionY();

        if (headPositionX == positionX && positionY == headPositionY) {
            growSnake();
            addPoint();
        } else {
            // Yılanın kuyruğunu kısalt
            snakePointsList.remove(snakePointsList.size() - 1);
        }

        switch (movingPosition) {
            case "right":
                headPositionX += (pointSize * 2);
                break;
            case "left":
                headPositionX -= (pointSize * 2);
                break;
            case "top":
                headPositionY -= (pointSize * 2);
                break;
            case "bottom":
                headPositionY += (pointSize * 2);
                break;
        }

        snakePointsList.add(0, new SnakePoints(headPositionX, headPositionY));

        if (checkGameOver(headPositionX, headPositionY)) {
            if (handler != null && runnable != null) {
                handler.removeCallbacks(runnable);
            }
            showGameOverDialog();
        } else {
            drawSnakeAndPoint();
        }
    }

    private void growSnake() {
        score++;
        scoreTV.setText(String.valueOf(score));
    }

    private boolean checkGameOver(int headPositionX, int headPositionY) {
        if (headPositionX < 0 || headPositionY < 0 || headPositionX >= surfaceView.getWidth() || headPositionY >= surfaceView.getHeight()) {
            return true;
        } else {
            for (int i = 1; i < snakePointsList.size(); i++) {
                if (headPositionX == snakePointsList.get(i).getPositionX() && headPositionY == snakePointsList.get(i).getPositionY()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void drawSnakeAndPoint() {
        if (surfaceHolder == null) return;

        canvas = surfaceHolder.lockCanvas();
        if (canvas == null) return;

        canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
        Paint paint = createPointColor();

        for (SnakePoints point : snakePointsList) {
            canvas.drawCircle(point.getPositionX(), point.getPositionY(), pointSize, paint);
        }

        canvas.drawCircle(positionX, positionY, pointSize, paint);

        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    private Paint createPointColor() {
        if (pointColor == null) {
            pointColor = new Paint();
            pointColor.setColor(snakeColor);
            pointColor.setStyle(Paint.Style.FILL);
            pointColor.setAntiAlias(true);
        }
        return pointColor;
    }

    private void showGameOverDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Your Score = " + score);
        builder.setTitle("Game Over");
        builder.setCancelable(false);
        builder.setPositiveButton("Start Again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss(); // Dialog penceresini kapat
                handler.removeCallbacks(runnable); // Önceki oyunun hızını sıfırla
                init(); // Oyunu yeniden başlat
            }
        });
        builder.show();
    }
}
