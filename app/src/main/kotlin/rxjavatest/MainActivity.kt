package rxjavatest

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.reactivex.Observable
import io.reactivex.annotations.NonNull
import io.reactivex.functions.BiFunction
import rxjava.bluewhale.kr.rxjavatest.R

/**
 * Created by tyler on 2017. 8. 5..
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Observable.just(1)
                .zipWith(Observable.just(2), BiFunction<Int, Int, String> { t1, t2 ->
                    "asd"
                })

    }
}