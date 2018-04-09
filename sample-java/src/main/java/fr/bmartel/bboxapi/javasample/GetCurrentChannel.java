package fr.bmartel.bboxapi.javasample;

import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Handler;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;
import com.github.kittinunf.result.Result;
import de.mannodermaus.rxbonjour.platforms.desktop.DesktopPlatform;
import fr.bmartel.bboxapi.stb.BboxApiStb;
import fr.bmartel.bboxapi.stb.model.Media;
import kotlin.Triple;
import kotlin.Unit;

public class GetCurrentChannel {

    public static void main(String args[]) {
        BboxApiStb bboxapi = new BboxApiStb("YourAppId", "YourAppSecret");

        bboxapi.startRestDiscovery(true, DesktopPlatform.create(), 10000, (stbServiceEvent, stbService, throwable) -> {
            switch (stbServiceEvent) {
                case SERVICE_FOUND:
                    System.out.println("service found : " + stbService.getIp() + ":" + stbService.getPort());

                    bboxapi.getCurrentChannel(new Handler<Media>() {
                        @Override
                        public void success(Request request, Response response, Media channel) {
                            System.out.println(channel);
                        }

                        @Override
                        public void failure(Request request, Response response, FuelError fuelError) {
                            fuelError.printStackTrace();
                        }
                    });

                    Triple<Request, Response, Result<Media, FuelError>> data = bboxapi.getCurrentChannelSync();
                    Request request = data.getFirst();
                    Response response = data.getSecond();
                    Result<Media, FuelError> obj = data.getThird();
                    System.out.println(obj.get());
                    break;
                case DISCOVERY_STOPPED:
                    System.out.println("end of discovery");
                    break;
                case DISCOVERY_ERROR:
                    throwable.printStackTrace();
                    break;
            }
            return Unit.INSTANCE;
        });
    }
}
