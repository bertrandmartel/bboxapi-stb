package fr.bmartel.bboxapi.javasample;

import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Handler;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;
import com.github.kittinunf.result.Result;
import de.mannodermaus.rxbonjour.platforms.desktop.DesktopPlatform;
import fr.bmartel.bboxapi.stb.BboxApiStb;
import fr.bmartel.bboxapi.stb.model.Channel;
import kotlin.Triple;
import kotlin.Unit;

import java.util.List;

public class GetAppIcon {

    public static void main(String args[]) {
        BboxApiStb bboxapi = new BboxApiStb("YourAppId", "YourAppSecret");

        bboxapi.startRestDiscovery(true, DesktopPlatform.create(), 10000, (stbServiceEvent, stbService, throwable) -> {
            switch (stbServiceEvent) {
                case SERVICE_FOUND:
                    System.out.println("service found : " + stbService.getIp() + ":" + stbService.getPort());

                    bboxapi.getAppIcon("com.google.android.youtube.tv", new Handler<byte[]>() {
                        @Override
                        public void success(Request request, Response response, byte[] image) {
                            System.out.println(image.length);
                        }

                        @Override
                        public void failure(Request request, Response response, FuelError fuelError) {
                            fuelError.printStackTrace();
                        }
                    });

                    Triple<Request, Response, Result<byte[], FuelError>> data = bboxapi.getAppIconSync("com.google.android.youtube.tv");
                    Request request = data.getFirst();
                    Response response = data.getSecond();
                    Result<byte[], FuelError> obj = data.getThird();
                    System.out.println(obj.get().length);
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