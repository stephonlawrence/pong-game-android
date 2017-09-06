package com.example.stephonlawrence.ponggame;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;
import java.util.Timer;
import java.util.Date;

import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tanh;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

public class Game extends View {
    private Player player1, player2;
    private Ball ball;
    private GameState gameState;
    private Timer gameLoopTimer;
    private Date lastTime;
    private float canvasWidth, canvasHeight;
    private Paint background;

    public Game(Context context) {
        super(context);
        init(null, 0);
    }

    public Game(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public Game(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        lastTime = new Date();
        background = new Paint();
        background.setColor(Color.BLACK);
        gameState = GameState.beforestart;
        player1 = new Player(Side.left);
        player2 = new Player(Side.right);
        ball = new Ball();
        gameLoopTimer = new Timer();
        gameLoopTimer.schedule(
                new java.util.TimerTask() {
                    public void run() {
                        if(gameState == GameState.playing) {
//                            Log.d("update_timer", "updated");
                            update();
                        }
                    }
                },
                0,
                1000/60
        );
    }

    class Position {
        float x;
        float y;
    }

    private Side checkWallCollision() {
        // check for wall collision
        // left wall
        if(ball.getEdge(180).x <= 0 )
            return Side.left;
//         top wall
        if(ball.getEdge(270).y <= 0)
            return Side.top;
        // bottom wall
        if(ball.getEdge(90).y >= canvasHeight)
            return Side.bottom;
        // right wall
        if(ball.getEdge(0).x >= canvasWidth)
            return Side.right;
        return null;
    }

    private void update() {
        Side s = checkWallCollision();
        // https://www.safaribooksonline.com/library/view/html5-canvas/9781449308032/ch05s02.html
        if(s != null) {
            float direction = ball.direction % 360;
            if(s == Side.left || s == Side.right) {
                ball.direction = 180 - direction;
            }
            if(s == Side.top || s == Side.bottom) {
                ball.direction = 360 - direction;
            }
        }
        if(player1.isCollidingWithBall(ball.pos, ball.radius)) {
            if(ball.pos.y < player1.start.y + player1.height / 2) ball.direction = 315f;
            if(ball.pos.y > player1.start.y + player1.height / 2) ball.direction = 45f;
            if(ball.pos.y == player1.start.y + player1.height / 2) ball.direction = 0f;
        }
        if(player2.isCollidingWithBall(ball.pos, ball.radius)) {
            if(ball.pos.y < player2.start.y + player2.height / 2) ball.direction = 225f;
            if(ball.pos.y > player2.start.y + player2.height / 2) ball.direction = 135f;
            if(ball.pos.y == player2.start.y + player2.height / 2) ball.direction = 180f;
        }
        ball.moveForward();
        postInvalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int index = event.getActionIndex();
        int maskedAction = event.getActionMasked();
        Position pos = new Position();
        int pointerIndex;

        switch (maskedAction) {
            case MotionEvent.ACTION_DOWN: {
                pointerIndex = event.getPointerId(index);
                pos.x = event.getX();
                pos.y = event.getY();
                if(player1.controllerIndex == null && player1.isColliding(pos)) {
                    player1.setController(pointerIndex, pos);
                }
                else if(player2.controllerIndex == null && player2.isColliding(pos)) {
                    player2.setController(pointerIndex, pos);
                }
                Log.v("action", "normal = i: "+index+" | x: "+pos.x+" | y: "+pos.y);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int pointerCount = event.getPointerCount();
                for(int i = 0; i < pointerCount; ++i) {
                    pointerIndex = event.getPointerId(i);
                    pos.x = event.getX(i);
                    pos.y = event.getY(i);
                    if(player1.controllerIndex != null && pointerIndex == player1.controllerIndex) {
                        player1.controllerMove(pos);
                    }
                    else if(player2.controllerIndex != null && pointerIndex == player2.controllerIndex) {
                        player2.controllerMove(pos);
                    }
                }
//                Log.v("action", "move = pI: "+pointerIndex+" | i: "+index+" | x: "+pos.x+" | y: "+pos.y);
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                pointerIndex = event.getPointerId(index);
                if(player1.controllerIndex == null && player1.isColliding(pos))
                    player1.setController(pointerIndex, pos);
                else if(player2.controllerIndex == null && player2.isColliding(pos))
                    player2.setController(pointerIndex, pos);

                pos.x = event.getX();
                pos.y = event.getY();
                Log.v("action", "pointer = i: "+index+" | x: "+pos.x+" | y: "+pos.y);
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                pointerIndex = event.getPointerId(index);
                if(player1.controllerIndex != null && player1.controllerIndex == pointerIndex) {
                    player1.controllerIndex = null;
                }
                if(player2.controllerIndex != null && player2.controllerIndex == pointerIndex) {
                    player2.controllerIndex = null;
                }
            }
            case MotionEvent.ACTION_UP: {
                pointerIndex = event.getPointerId(index);
                if(player1.controllerIndex != null && player1.controllerIndex == pointerIndex) {
                    player1.controllerIndex = null;
                }
                if(player2.controllerIndex != null && player2.controllerIndex == pointerIndex) {
                    player2.controllerIndex = null;
                }
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(gameState == GameState.beforestart) {
            canvasWidth = canvas.getWidth();
            canvasHeight = canvas.getHeight();

        }
        canvas.drawRect(0, 0, canvasWidth, canvasHeight, background);
        player1.draw(canvas);
        player2.draw(canvas);
        ball.draw(canvas);
        if(gameState == GameState.beforestart) {
            gameState = GameState.playing;
        }
    }

    enum GameState {
        beforestart,
        playing,
        paused,
        end
    }

    enum Side {
        left, right, bottom, top
    }

    class Player {
        private float width= 150, height = 400;
        private Paint paint;
        private Side side;
        private Integer controllerIndex;
        private Position controllerPos;
        private Position start;
        private Position end;



        Player(Side side) {
            this.side = side;
            start  = new Position();
            end = new Position();
            paint = new Paint();
            paint.setColor(Color.BLUE);
            controllerPos = new Position();
        }

        public boolean isColliding(Position place) {
            return place.x >= start.x && place.x <= end.x && place.y >= start.y && place.y <= end.y;
        }

        public boolean isCollidingWithBall(Position circlePos, float radius) {
            float distx = Math.abs(circlePos.x - (start.x+width/2));
            float disty = Math.abs(circlePos.y - (start.y+height/2));

            if(distx > width/2 + radius) return false;
            if(disty > height/2 + radius)return false;

            if(distx <= width/2) return true;
            if(disty <= height/2) return true;

            float dx = distx - width/2, dy = disty - height/2;
            return dx * dy + dy * dy <= radius * radius;
        }

        public void setController(int index, Position pos) {
            controllerIndex = index;
            controllerPos.x = pos.x;
            controllerPos.y = pos.y;
        }
        public void controllerMove(Position pos) {
            float dy = (pos.y - controllerPos.y);
            start.y += dy;
            controllerPos.y = pos.y;
//            start.y = pos.y - (controllerPos.y - start.y);
//            Log.v("move", "i = "+controllerIndex+"| dy = " + dy);
        }

        public void draw(Canvas canvas) {
            if(gameState == GameState.beforestart) {
                if(side == Side.left) {
                    start.x = 0;
                    start.y = 0;
                }
                if(side == Side.right) {
                    start.x = canvasWidth - width;
                    start.y = 0;
                }
            }
            if(start.y < 0) start.y = 0;
            if(start.y + height > canvasHeight) start.y = canvasHeight - height;
            end.x = start.x + width;
            end.y = start.y + height;
            canvas.drawRect(start.x, start.y, end.x, end.y, paint);
        }
    }

    class Ball {
        private Position pos;
        private float radius = 100;
        private int color;
        private Paint paint;
        private float direction;
        private float speed = 20f;

        Ball() {
            paint = new Paint();
            color = Color.RED;
            paint.setColor(color);
            direction = 125f;//new Random().nextFloat() * 360;
            pos = new Position();
        }

        public void draw(Canvas canvas) {
            if(gameState == GameState.beforestart) {
                pos.x = (canvas.getWidth() / 2) - (radius / 2);
                pos.y = (canvas.getHeight() / 2) - (radius / 2);
            }
            canvas.drawCircle(pos.x, pos.y, radius, paint);
        }
        public void moveForward() {
            pos.x += speed * cos(toRadians(direction));
            pos.y += speed * sin(toRadians(direction));
        }
        public void rotate(float degrees) {
            direction = (direction + degrees) % 360;
        }


        public Position getEdge(float degrees) {
            Position pos = new Position();
            pos.x = this.pos.x + (this.radius * (float) cos(toRadians(degrees)));
            pos.y = this.pos.y + (this.radius * (float) sin(toRadians(degrees)));
            return pos;
        }
    }

}
