package rxjava.bluewhale.kr.rxjavatest

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.annotations.NonNull
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.zipWith
import org.hamcrest.core.IsCollectionContaining.hasItem
import org.hamcrest.core.IsNull.notNullValue
import org.junit.Assert.assertThat
import org.junit.Test
import java.util.*
import io.reactivex.observers.TestObserver
import io.reactivex.plugins.RxJavaPlugins
import javax.xml.datatype.DatatypeConstants.SECONDS
import io.reactivex.schedulers.Schedulers
import org.awaitility.Awaitility
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers
import java.util.concurrent.TimeUnit
import javax.xml.datatype.DatatypeConstants.SECONDS
import org.junit.Rule
import io.reactivex.plugins.RxJavaPlugins.setComputationSchedulerHandler
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit.*
import javax.xml.datatype.DatatypeConstants.SECONDS


/**
 * Created by tyler on 2017. 8. 5..
 */
class FirstStep {

    private val WORDS = arrayListOf("the",
            "quick",
            "brown",
            "fox",
            "jumped",
            "over",
            "the",
            "lazy",
            "dog")


    @Test
    fun testInSameThread() {

        val observer = TestObserver<String>()
        // given:
        val observable = Observable.fromIterable(WORDS)
                .zipWith(Observable.range(1, Integer.MAX_VALUE),
                        BiFunction<String, Int, String> { words, index ->
                            "${index}.${words}"
                        })
                // when:
                .subscribe(observer)
        observer.assertNoErrors()
        observer.assertComplete()
        observer.assertValueCount(9)
        observer.assertValueAt(4, { value ->
            value.equals("5.jumped")
        })
    }

    @Test
    fun testFailure() {
        // given :
        val observer = TestObserver<String>()
        val exception = RuntimeException("boom!")

        val observable = Observable.fromIterable(WORDS)
                .zipWith(Observable.range(1, Integer.MAX_VALUE)
                ) { string, index -> String.format("%2d. %s", index, string) }
                .concatWith(Observable.error(exception))
                // when :
                .subscribe(observer)


        observer.assertError(exception)

        observer.assertNotComplete()
    }

    @Test
    fun testUsingComputationScheduler() {
        // given:
        val observer = TestObserver<String>()
        val observable = Observable.fromIterable(WORDS)
                .zipWith(Observable.range(1, Integer.MAX_VALUE),
                        BiFunction<String, Int, String> { words, index ->
                            "${index}.${words}"
                        })

        // when:
        observable.subscribeOn(Schedulers.computation())
                .subscribe(observer)

//        Awaitility.await().timeout(2, TimeUnit.SECONDS)
//                .until { observer.valueCount() == 9  }
        observer.awaitTerminalEvent(2, TimeUnit.SECONDS)
        // then:
        observer.assertComplete()
        observer.assertNoErrors()
        assertThat(observer.values(), hasItem("4.fox"))
    }

    @Test
    fun testUsingBlockingCall() {
        // given:
        val observable = Observable.fromIterable(WORDS)
                .zipWith(Observable.range(1, Integer.MAX_VALUE),
                        BiFunction<String, Int, String> { words, index ->
                            "${index}.${words}"
                        })

        // when:
        val results = observable
                .subscribeOn(Schedulers.computation())
                .blockingIterable()

        // then:
        assertThat(results, notNullValue())
        assertThat(results, Matchers.iterableWithSize(9))
        assertThat(results, hasItem("4.fox"))
    }

    @Test
    fun testUsingComputationScheduler2() {
        // given:
        val observer = TestObserver<String>()
        val observable = Observable.fromIterable(WORDS)
                .zipWith(Observable.range(1, Integer.MAX_VALUE),
                        BiFunction<String, Int, String> { words, index ->
                            "${index}.${words}"
                        })

        // when:
        observable.subscribeOn(Schedulers.computation())
                .subscribe(observer)

        observer.awaitTerminalEvent(2, TimeUnit.SECONDS)

        // then:
        observer.assertComplete()
        observer.assertNoErrors()
        assertThat(observer.values(), hasItem("4.fox"))
    }

    @Test
    fun testUsingRxJavaPluginsWithImmediateScheduler() {
        RxJavaPlugins.setComputationSchedulerHandler({ scheduler ->
            Schedulers.trampoline()
        })
        // given:
        val observer = TestObserver<String>()
        val observable = Observable.fromIterable(WORDS)
                .zipWith(Observable.range(1, Integer.MAX_VALUE),
                        BiFunction<String, Int, String> { words, index ->
                            "${index}.${words}"
                        })

        // when:
        observable.subscribeOn(Schedulers.computation())
                .subscribe(observer)


        // then:
        observer.assertComplete()
        observer.assertNoErrors()
        assertThat(observer.values(), hasItem("4.fox"))
    }


    @Test
    fun testUsingTestScheduler() {
        // given:
        val scheduler = TestScheduler()
        val observer = TestObserver<String>()
        val tick = Observable.interval(1, TimeUnit.SECONDS, scheduler)

        val observable = Observable.fromIterable(WORDS)
                .zipWith(Observable.range(1, Integer.MAX_VALUE),
                        BiFunction<String, Int, String> { words, index ->
                            "${index}.${words}"
                        })
        observable.subscribeOn(scheduler)
                .subscribe(observer);

//        expect
        observer.assertNoValues();
        observer.assertNotComplete();

//        when
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);

        // then:
        observer.assertComplete()
        observer.assertValueCount(1)
        assertThat(observer.values(), hasItem("1.the"))

        //        when
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);

//        then
        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValueCount(9)

    }
}
