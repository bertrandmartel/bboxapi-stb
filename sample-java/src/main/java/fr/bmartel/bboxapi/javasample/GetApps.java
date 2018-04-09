package fr.bmartel.bboxapi.javasample;

import com.github.kittinunf.fuel.core.*;
import com.github.kittinunf.result.Result;
import de.mannodermaus.rxbonjour.platforms.desktop.DesktopPlatform;
import fr.bmartel.bboxapi.stb.BboxApiStb;
import fr.bmartel.bboxapi.stb.model.Application;
import kotlin.Triple;
import kotlin.Unit;

import java.util.List;

public class GetApps {

    public static void main(String args[]) {
        BboxApiStb bboxapi = new BboxApiStb("YourAppId", "YourAppSecret");

        bboxapi.startRestDiscovery(true, DesktopPlatform.create(), 10000, (stbServiceEvent, stbService, throwable) -> {
            switch (stbServiceEvent) {
                case SERVICE_FOUND:
                    System.out.println("service found : " + stbService.getIp() + ":" + stbService.getPort());

                    bboxapi.getApps(new Handler<List<Application>>() {
                        @Override
                        public void failure(Request request, Response response, FuelError error) {
                            if (error.getException() instanceof HttpException) {
                                System.out.println("http error : " + response.getStatusCode());
                            } else {
                                error.printStackTrace();
                            }
                        }

                        @Override
                        public void success(Request request, Response response, List<Application> data) {
                            System.out.println(data);
                        }
                    });

                    Triple<Request, Response, Result<List<Application>, FuelError>> data = bboxapi.getAppsSync();
                    Request request = data.getFirst();
                    Response response = data.getSecond();
                    Result<List<Application>, FuelError> obj = data.getThird();
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
