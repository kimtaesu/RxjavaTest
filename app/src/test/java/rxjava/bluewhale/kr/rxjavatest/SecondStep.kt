package rxjava.bluewhale.kr.rxjavatest

import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import org.junit.Test
import io.reactivex.observers.TestObserver
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.Rule
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by tyler on 2017. 8. 5..
 */
class SecondStep {

    private val WORDS = Arrays.asList(
            "the",
            "quick",
            "brown",
            "fox",
            "jumped",
            "over",
            "the",
            "lazy",
            "dog"
    )

    class TestSchedulerRule : TestRule {
        public val testScheduler = TestScheduler()

        override fun apply(base: Statement, description: Description): Statement {
            return object : Statement() {
                @Throws(Throwable::class)
                override fun evaluate() {
                    RxJavaPlugins.setIoSchedulerHandler { scheduler -> testScheduler }
                    RxJavaPlugins.setComputationSchedulerHandler { scheduler -> testScheduler }
                    RxJavaPlugins.setNewThreadSchedulerHandler { scheduler -> testScheduler }

                    try {
                        base.evaluate()
                    } finally {
                        RxJavaPlugins.reset()
                    }
                }
            }
        }
    }

    @Rule @JvmField
    public val testSchedulerRule = TestSchedulerRule()

    @Test
    fun testUsingTestSchedulersRule() {
        // given:
        val observer = TestObserver<String>()

        val observable = Observable.fromIterable(WORDS)
                .zipWith(Observable.interval(2, TimeUnit.SECONDS),
                        BiFunction<String, Long, String> { words, index ->
                            "${index}.${words}"
                        })

        observable.subscribeOn(Schedulers.computation())
                .subscribe(observer)

        // expect
        observer.assertNoValues()
        observer.assertNotComplete()

        // when:
        testSchedulerRule.testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)

        // then:
        observer.assertNoErrors()
        observer.assertValueCount(1)
        observer.assertValues("0.the")

        // when:
        testSchedulerRule.testScheduler.advanceTimeTo(18, TimeUnit.SECONDS)
        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValueCount(9)
    }
}