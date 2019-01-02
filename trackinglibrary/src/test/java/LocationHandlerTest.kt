import android.location.Location
import com.trackinglibrary.BuildConfig
import com.trackinglibrary.services.LocationHandler
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, manifest = Config.NONE)
class LocationHandlerTest {

    @Test(expected = IllegalArgumentException::class)
    fun `should throw exception non valid save frequency`() {
        LocationHandler(1000, 0, {}, {})
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw exception non valid last saved time`() {
        LocationHandler(TimeUnit.MINUTES.toMillis(10), -1, {}, {})
    }

    @Test
    fun `should call saveTimeAndDistance on new location`() {

        // given
        val locationHandler = spy(LocationHandler(TimeUnit.MINUTES.toSeconds(10), 0, {}, {}))

        val loc1 = createLocation(33.1, 44.1, 10f, 0L)
        val loc2 = createLocation(33.5, 44.5, 10f, TimeUnit.SECONDS.toMillis(10))
        val loc3 = createLocation(33.7, 44.8, 10f, TimeUnit.SECONDS.toMillis(50))

        // #1 when
        locationHandler.newLocation(loc1)

        // then
        // save time and distance called with empty first location
        verify(locationHandler, times(1)).saveTimeAndDistance(null, loc1)

        // #2 when
        locationHandler.newLocation(loc2)

        // then
        // save time and distance between first and second location
        verify(locationHandler, times(1)).saveTimeAndDistance(loc1, loc2)

        // #3 when
        locationHandler.newLocation(loc3)

        // then
        // save time and distance between second and third locations
        verify(locationHandler, times(1)).saveTimeAndDistance(loc2, loc3)

        //
        verify(locationHandler, times(3)).saveTimeAndDistance(any(), any())
    }

    @Test
    fun `should skip bad location`() {

        // given
        val locationHandler = spy(LocationHandler(TimeUnit.MINUTES.toSeconds(10), 0, {}, {}))
        val badAccLocation = createBadAccuracyLocation()

        // when
        locationHandler.newLocation(badAccLocation)

        // then
        verify(locationHandler, never()).saveTimeAndDistance(any(), any())
    }

    @Test
    fun `should notify new interval location`() {
        // given
        val locationHandler = spy(LocationHandler(TimeUnit.MINUTES.toMillis(10), 0, {}, {}))
        val loc1 = createLocation(2.0, 2.0, 10f, TimeUnit.MINUTES.toMillis(1))

        // when
        locationHandler.newLocation(loc1)

        // then
        // should notify new location
        verify(locationHandler, times(1)).notifyNewLocation(any())

        // when
        val loc2 = createLocation(2.0, 2.0, 10f, TimeUnit.MINUTES.toMillis(2))
        locationHandler.newLocation(loc2)

        // then
        // should not save location, 10 mins between intervals
        verify(locationHandler, times(1)).notifyNewLocation(any())

        // when
        val loc3 = createLocation(2.0, 2.0, 10f, TimeUnit.MINUTES.toMillis(10))
        locationHandler.newLocation(loc3)

        // then
        // should not save location, 9 mins passed
        verify(locationHandler, times(1)).notifyNewLocation(any())

        // when
        val loc4 = createLocation(2.0, 2.0, 10f, TimeUnit.MINUTES.toMillis(11))
        locationHandler.newLocation(loc4)

        // then
        // should not save location, 1o mins passed
        verify(locationHandler, times(2)).notifyNewLocation(any())
    }

    private fun createLocation(lat: Double, lon: Double, acc: Float, date: Long): Location {
        val location = mock(Location::class.java)
        `when`(location.latitude).thenReturn(lat)
        `when`(location.longitude).thenReturn(lon)
        `when`(location.accuracy).thenReturn(acc)
        `when`(location.time).thenReturn(date)
        `when`(location.hasAccuracy()).thenReturn(true)
        return location
    }

    private fun createBadAccuracyLocation(): Location {
        val location = mock(Location::class.java)
        `when`(location.accuracy).thenReturn(30f)
        `when`(location.hasAccuracy()).thenReturn(true)
        return location
    }
}