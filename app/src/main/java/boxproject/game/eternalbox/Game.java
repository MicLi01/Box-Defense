package boxproject.game.eternalbox;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Game extends AppCompatActivity {
    private int score = 0;
    private int life;
    private int highScore;
    private int height;
    private int width;
    private int speedx;
    private int speedy;
    private Timer timer = new Timer();
    private Handler handler = new Handler();
    private Timer moveTimer = new Timer();
    private Handler moveHandler = new Handler();
    private int numEnemy = 0;
    ConstraintSet set = new ConstraintSet();
    ConstraintLayout layout;
    ImageView box;
    ArrayList<Integer> posId = new ArrayList<>();
    ArrayList<ImageView> enemies = new ArrayList<>();
    ArrayList<ImageView> activeEnemies = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        layout = findViewById(R.id.layoutScr);
        box = findViewById(R.id.box);
        Intent intent = getIntent();
        life = intent.getIntExtra("lives",0);
        highScore = intent.getIntExtra("highScore",0);
        findViewById(R.id.box).setTag(3);
        ((TextView)findViewById(R.id.score)).setText(String.valueOf(score));
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;
        genSpeed();
        initGameData();
        enemySchedule();
        enemyScheduleMove();
    }

    private void genSpeed(){
        Timer speedTimer = new Timer();
        final Handler speedHandler = new Handler();
        speedTimer.schedule(new TimerTask() {
            public void run() {
                speedHandler.post(new Runnable() {
                    public void run() {
                        int[] xyBox = new int[2];
                        box.getLocationOnScreen(xyBox);
                        speedx = (int)(xyBox[0] * .1);
                        speedy = (int)(xyBox[1] * .1);
                    }
                });
            }
        }, 100);
    }

    private void initGameData(){
        genImgViews();
        genIds();
    }

    private void genImgViews(){
        for(int i = 0; i < 10; i++){
            genView(i);
        }
    }

    private void genView(int id){
        ImageView newEnemy = new ImageView(this);
        newEnemy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enemyClick(view);
            }
        });
        ConstraintLayout layout = findViewById(R.id.layoutScr);
        newEnemy.setId(id);
        enemies.add(newEnemy);
        layout.addView(newEnemy, 0);
    }

    private void setViewData(ImageView current){
        int randomNum = (int)(Math.random() * 2);
        if(randomNum == 0){
            current.setTag(1);
            //Insert X img here
        }
        else if(randomNum == 1){
            current.setTag(2);
            //Insert O img here
        }
        current.setVisibility(View.VISIBLE);
    }

    private void genIds(){
        for(int i = 0; i < 10; i++){
            posId.add(i);
        }
    }

    private void enemySchedule(){
        timer.schedule(new TimerTask() {
                public void run() {
                    handler.post(new Runnable() {
                        public void run() {
                            if(enemies.size() > 0) {
                                //newEnemy();
                                genLoc();
                            }
                            enemySchedule();
                        }
                    });
                }
            }, 100);
        checkLose();
    }

    private void enemyScheduleMove(){
        moveTimer.schedule(new TimerTask() {
            public void run() {
                moveHandler.post(new Runnable() {
                    public void run() {
                        for(int i = 0; i < activeEnemies.size(); i++){
                            moveView(activeEnemies.get(i));
                        }
                        enemyScheduleMove();
                    }
                });
            }
        }, 100);
    }

    private void gameEnd(){
        timer.cancel();
        timer.purge();
        if(score > highScore){
            update();
        }
    }

    private void update(){
        SharedPreferences prefs = this.getSharedPreferences("EBData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("score", score);
        editor.commit();
    }

    public void enemyClick(View view){
        int tag = (int)(view.getTag());
        tag -= 1;
        if(tag == 0){
            updateScore();
            view.setVisibility(View.INVISIBLE);
            enemies.add((ImageView)view);
            activeEnemies.remove((ImageView)view);
            //((TextView)findViewById(R.id.textView2)).setText(String.valueOf(enemies.size()));
            posId.add(view.getId());
        }
        view.setTag(tag);
    }

    private void updateScore(){
        score += 1;
        ((TextView)findViewById(R.id.score)).setText(String.valueOf(score));
    }

    private void checkLose(){
        int hp = (int) findViewById(R.id.box).getTag();
        if(hp == 0){
            gameEnd();
        }
    }

    private void newEne(ImageView newEn, int measuredH, int measuredW){
        int side = (int)(Math.random() * 2);
        if(side == 0){
            genSide(newEn, measuredW, measuredH);
        }
        else{
            genTopBot(newEn, measuredW, measuredH);
        }
    }

    private void genSide(ImageView newEn, int measuredW, int measuredH){
        int wSide = (int)(Math.random() * 2);
        int xCoord = 0;
        int yCoord = (int)(Math.random() * (height - measuredH));
        if(wSide == 0){
            xCoord = 0;
        }
        else{
            xCoord = width - measuredW;
        }
        //genLoc(xCoord, yCoord);
        setConstr(newEn, xCoord, yCoord);
    }

    private void genTopBot(ImageView newEn, int measuredW, int measuredH){
        int wSide = (int)(Math.random() * 2);
        int xCoord = (int)(Math.random() * (width - measuredW));
        int yCoord;
        if(wSide == 0){
            yCoord = 0;
        }
        else{
            yCoord = height - measuredH;
        }
        //genLoc(xCoord, yCoord);
        setConstr(newEn, xCoord, yCoord);
    }

    private void setConstr(ImageView newEn, int x, int y){
        set.clone(layout);
        set.connect(newEn.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, x);
        set.connect(newEn.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, y);
        set.applyTo(layout);
        newEn.setEnabled(true);
    }

    private void genLoc(){
        //ImageView newEnemy = new ImageView(this);
        //newEnemy.setId(posId.get(0));
        //posId.remove(0);
        ImageView newEnemy = enemies.get(0);
        activeEnemies.add(enemies.get(0));
        enemies.remove(0);
        setViewData(newEnemy);
        //newEnemy.setImageResource(R.color.colorAccent);
        newEnemy.setImageResource(R.drawable.ic_launcher_background);
        int measuredHeight = newEnemy.getMeasuredHeight();
        int measuredWidth = newEnemy.getMeasuredWidth();
        newEne(newEnemy, measuredHeight, measuredWidth);
       //ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) newEnemy.getLayoutParams();
        //ConstraintLayout layout = findViewById(R.id.layoutScr);
        //ConstraintSet set = new ConstraintSet();
        //ConstraintSet set = new ConstraintSet();
        //ConstraintLayout layout = findViewById(R.id.layoutScr);
        //layout.addView(newEnemy, 0);
        //setContentView(layout);
        //set.clone(layout);
        //set.constrainHeight(newEnemy.getId(), ConstraintSet.WRAP_CONTENT);
        //set.constrainWidth(newEnemy.getId(), ConstraintSet.WRAP_CONTENT);
        //set.connect(newEnemy.getId(), ConstraintSet.START, layout.getId(), ConstraintSet.START, xCoord);
        //set.connect(newEnemy.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP, yCoord);
        //set.connect(layout.getId(), ConstraintSet.START, newEnemy.getId(), ConstraintSet.START, 60);
        //set.setMargin(newEnemy.getId(), ConstraintSet.START, xCoord);
        //set.connect(layout.getId(), ConstraintSet.TOP, newEnemy.getId(), ConstraintSet.TOP, 60);
        //set.setMargin(newEnemy.getId(), ConstraintSet.TOP, yCoord);
        //set.connect(newEnemy.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, xCoord);
        //set.connect(newEnemy.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, yCoord);
        //set.connect(newEnemy.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0);
        //set.connect(newEnemy.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);
        //set.applyTo(layout);
        //newEnemy.setEnabled(true);
        //((TextView)findViewById(R.id.textView)).setText(String.valueOf(xCoord));
        //((TextView)findViewById(R.id.textView2)).setText(String.valueOf(yCoord));
        //final float scale = this.getResources().getDisplayMetrics().density;
        //marginParams.topMargin = yCoord;
        //marginParams.leftMargin = xCoord;
        //newEnemy.setLayoutParams(marginParams);
        /*newEnemy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enemyClick(view);
            }
        });*/
        //newEnemy.setLayoutParams(new ViewGroup.LayoutParams(20, 20));
        //((TextView)findViewById(R.id.textView2)).setText(String.valueOf(enemies.size()));
        //((TextView)findViewById(R.id.textView2)).setText(String.valueOf(layout.getChildCount()));
    }

    private void moveView(ImageView choice){
        int[] xyBox = new int[2];
        int[] xyChoice = new int [2];
        //int xdiff = Math.abs(xyBox[0] - xyChoice[0]);
        //int ydiff = Math.abs(xyBox[1] - xyChoice[1]);
        moveViewDist(xyBox, xyChoice, choice);
    }

    private void moveViewDist(int[] xyBox, int[] xyChoice, ImageView choice){
        //need to calculate speed
        box.getLocationOnScreen(xyBox);
        choice.getLocationOnScreen(xyChoice);
        set.clone(layout);
        TextView tmp = findViewById(R.id.textView2);
        tmp.setText(String.valueOf(speedy));
        if(xyBox[0] - xyChoice[0] > 0){
            set.connect(choice.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, xyChoice[0] + speedx);
        }
        else{
            set.connect(choice.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, xyChoice[0] - speedx);
        }
        if(xyBox[1] - xyChoice[1] > 0){
            set.connect(choice.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, xyChoice[1] + speedy);
        }
        else{
            set.connect(choice.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, xyChoice[1] - speedy);
        }
        set.applyTo(layout);
    }

    private void increaseSpd(){

    }

    private void hideViews(){
        for(int i = 0; i < layout.getChildCount(); i++){
            View current = layout.getChildAt(i);
            current.setVisibility(View.INVISIBLE);
        }
    }

    private void displayPause(){

    }
}
