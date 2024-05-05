package com.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import androidx.annotation.Nullable;
public class MainActivity extends AppCompatActivity {

    EditText betNum1,
            betNum2,
            betNum3,
            betAmount;
    TextView betMultiplier,
            currentBalance,
            resultNum1,
            resultNum2,
            resultNum3,
            result;
    int multiplier = 2,
            defaultBetAmount = 1000,
            gameNumber = 1;
    Button setPlayBtn,
            resetBtn,
            reviewBtn;

    CustomAdapter customAdapter;
    ListView listView;

    List<String> history = new ArrayList<>();
    List<Integer> gameNumbers = new ArrayList<>();
    private static final int HISTORY_REQUEST_CODE = 1411;

    @SuppressLint({"DefaultLocale", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        resultNum1 = findViewById(R.id.resultNum1);
        resultNum2 = findViewById(R.id.resultNum2);
        resultNum3 = findViewById(R.id.resultNum3);
        betNum1 = findViewById(R.id.betNumber1);
        betNum2 = findViewById(R.id.betNumber2);
        betNum3 = findViewById(R.id.betNumber3);
        setPlayBtn = findViewById(R.id.setPlayButton);
        resetBtn = findViewById(R.id.resetButton);
        reviewBtn = findViewById(R.id.reviewButton);
        result = findViewById(R.id.textResult);
        betAmount = findViewById(R.id.betAmount);
        betMultiplier = findViewById(R.id.multiplier);
        currentBalance = findViewById(R.id.CurrentMoneyAmount);

        betMultiplier.setText(String.valueOf(multiplier));
        currentBalance.setText(String.valueOf(defaultBetAmount));

        customAdapter = new CustomAdapter(this, history, gameNumbers);
        listView = findViewById(R.id.lvItems);

        setPlayBtn.setOnClickListener(this::setBtn);

        resetBtn.setOnClickListener(this::resetBtn);

        reviewBtn.setOnClickListener(this::reviewHistory);

        listView.setVisibility(View.INVISIBLE);

        checkResetBtn();
    }

    private void checkResetBtn() {
        if (defaultBetAmount == 0 || currentBalance.getText().toString().equals("0")) {
            resetBtn.setEnabled(true);
        } else {
            resetBtn.setEnabled(false);
        }
    }

    public void resetBtn(View view) {
        betMultiplier.setText(String.valueOf(multiplier));
        int newDefault = 1000;
        defaultBetAmount = newDefault;
        currentBalance.setText(String.valueOf(defaultBetAmount));

        result.setText(null);
        betNum1.setText(null);
        betNum2.setText(null);
        betNum3.setText(null);
        betAmount.setText(null);
        resultNum1.setText(null);
        resultNum2.setText(null);
        resultNum3.setText(null);

        setPlayBtn.setText("Set");
        setPlayBtn.setBackgroundColor(Color.rgb(137, 207, 240));
        disableBetNums();

        checkResetBtn();
        Log.d("Reset Button", "Reset button clicked");
        listView.setVisibility(View.INVISIBLE);
    }

    private void updateBalance(int newBalance) {
        defaultBetAmount = newBalance;
        currentBalance.setText(String.valueOf(defaultBetAmount));
        checkResetBtn();
    }

    public void setBtn(View view) {
        if (setPlayBtn.getText().equals("Set")) {
            setPlayBtn.setText("Play");
            setPlayBtn.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(180, 200, 120)));
            enableBetNums();

        } else if (setPlayBtn.getText().equals("Play")){

            setPlayBtn.setText("Set");
            setPlayBtn.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(137, 207, 240)));

            if (betNumbersAmountEmpty()) {
                Toast.makeText(this, "Please input all required fields.", Toast.LENGTH_SHORT).show();
                return;
            } else if (betNumsEmpty()) {
                Toast.makeText(this, "Please input your bet numbers.", Toast.LENGTH_SHORT).show();
                return;
            } else if (betAmountEmpty()) {
                Toast.makeText(this, "Please input your bet amount.", Toast.LENGTH_SHORT).show();
                return;
            } else if (Integer.parseInt(betAmount.getText().toString()) > Integer.parseInt(currentBalance.getText().toString())) {
                Toast.makeText(this, "Insufficient balance. Please enter a lower bet amount.", Toast.LENGTH_SHORT).show();
                return;
            }
            else {
                twentyFivePrcntWinChance();
                gameResult();
                updateGameHistory();
            }
        }
    }

    private void enableBetNums() {
        betNum1.setEnabled(true);
        betNum2.setEnabled(true);
        betNum3.setEnabled(true);
        betAmount.setEnabled(true);
    }

    private void disableBetNums() {
        betNum1.setEnabled(false);
        betNum2.setEnabled(false);
        betNum3.setEnabled(false);
        betAmount.setEnabled(false);
    }

    private boolean betNumbersAmountEmpty() {
        return betNum1.getText().toString().isEmpty() &&
                betNum2.getText().toString().isEmpty() &&
                betNum3.getText().toString().isEmpty() &&
                betAmount.getText().toString().isEmpty();
    }

    private boolean betNumsEmpty() {
        return betNum1.getText().toString().isEmpty() &&
                betNum2.getText().toString().isEmpty() &&
                betNum3.getText().toString().isEmpty();
    }

    private boolean betAmountEmpty() {
        return betAmount.getText().toString().isEmpty();
    }

    public void generateNumbers(TextView... textViews){
        Random random = new Random();
        for (TextView textView : textViews) {
            int randomNum = random.nextInt(10);

            textView.setText(String.valueOf(randomNum));
        }
    }

    private int gamesPlayed = 0;
    private int wins = 0;


    public void twentyFivePrcntWinChance() {
        Random random = new Random();

        if (gamesPlayed > 0) {
            double winRate = calculateWinRate();
            if (winRate < 25) {
                resultNum1.setText(betNum1.getText().toString());
                resultNum2.setText(betNum2.getText().toString());
                resultNum3.setText(betNum3.getText().toString());
                wins++;
            } else {
                generateNumbers(resultNum1, resultNum2, resultNum3);
            }
        }
        gamesPlayed++;
    }

    public double calculateWinRate() {
        if (gamesPlayed == 0) {
            return 0.0; // Return 0 if no games have been played
        } else {
            return ((double) wins / gamesPlayed) * 100; // Calculate win rate
        }
    }

    public void gameResult() {
        boolean allMatch = true;
        String[] resultNums = {resultNum1.getText().toString(), resultNum2.getText().toString(), resultNum3.getText().toString()};
        String[] betNums = {betNum1.getText().toString(), betNum2.getText().toString(), betNum3.getText().toString()};

        for (int i = 0; i < resultNums.length; i++) {
            if (!resultNums[i].equals(betNums[i])) {
                allMatch = false;
                break;
            }
        }

        if (allMatch) {
            handleWin();
        } else {
            if(defaultBetAmount >= 0){
                handleLoss();
            }
        }
    }

    private void handleWin() {
        defaultBetAmount += Integer.parseInt(betAmount.getText().toString()) * multiplier;
        result.setText("YOU WIN!");
        updateBalance(defaultBetAmount);
        currentBalance.setText(String.valueOf(defaultBetAmount));
        multiplier++;
        betMultiplier.setText(String.valueOf(multiplier));
    }

    private void handleLoss() {
        int betAmountValue = Integer.parseInt(betAmount.getText().toString());
        if (defaultBetAmount >= betAmountValue) {
            result.setText("YOU LOSE!");
            defaultBetAmount -= betAmountValue;
            updateBalance(defaultBetAmount);
            currentBalance.setText(String.valueOf(defaultBetAmount));
            multiplier = 2;
            betMultiplier.setText(String.valueOf(multiplier));
        } else {
            Toast.makeText(this, "Insufficient balance. Please enter a lower bet amount.", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void updateGameHistory() {
        String gameOutcome = result.getText().toString();
        String gameMultiplier = betMultiplier.getText().toString();
        String balance = currentBalance.getText().toString();
        String bet = betAmount.getText().toString();
        String gameInfo = "Game #" + gameNumber + "\n" + "\n" + "Your Bet : " + bet + "\n" + gameOutcome + "\n" + "Multiplier : " + gameMultiplier + "\n" + "Balance : " + balance;
        history.add(gameInfo);
        gameNumbers.add(gameNumber);
        gameNumber++;
        customAdapter.notifyDataSetChanged();
    }

    private void reviewHistory(View view) {
        ListView listView = findViewById(R.id.lvItems);
        if (listView.getVisibility() == View.VISIBLE) {
            listView.setVisibility(View.INVISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
        }
        CustomAdapter adapter = new CustomAdapter(MainActivity.this,history,gameNumbers);
        listView.setAdapter(adapter);
        checkResetBtn();
    }

    class CustomAdapter extends BaseAdapter {
        private Context context;
        private List<String> history;
        private List<Integer> gameNumbers;

        public CustomAdapter(Context context, List<String> history, List<Integer> gameNumbers) {
            this.context = context;
            this.history = history;
            this.gameNumbers = gameNumbers;
        }

        @Override
        public int getCount() {
            return history.size();
        }

        @Override
        public Object getItem(int i) {
            return history.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.review_layout, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.gameNumber = convertView.findViewById(R.id.gameNumber);
                viewHolder.gameDetails = convertView.findViewById(R.id.gameDetails);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.gameNumber.setText(gameNumbers.get(i).toString());
            viewHolder.gameDetails.setText(history.get(i));

            return convertView;
        }

        private class ViewHolder {
            TextView gameNumber;
            TextView gameDetails;
        }
    }


}
