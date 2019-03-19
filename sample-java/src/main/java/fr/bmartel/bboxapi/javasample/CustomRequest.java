package fr.bmartel.bboxapi.javasample;

import com.github.kittinunf.fuel.core.*;
import com.github.kittinunf.result.Result;
import de.mannodermaus.rxbonjour.platforms.desktop.DesktopPlatform;
import fr.bmartel.bboxapi.stb.BboxApiStb;
import kotlin.Triple;
import kotlin.Unit;

public class CustomRequest {

    public static void main(String args[]) {
        BboxApiStb bboxapi = new BboxApiStb("YourAppId", "YourAppSecret");

        bboxapi.startRestDiscovery(true, DesktopPlatform.create(), 10000, (stbServiceEvent, stbService, changed, throwable) -> {
            switch (stbServiceEvent) {
                case SERVICE_FOUND:
                    System.out.println("service found : " + stbService.getIp() + ":" + stbService.getPort());
                    Triple<Request, Response, Result<byte[], FuelError>> data = bboxapi.createCustomRequestSync(bboxapi.getManager().request(Method.GET, "/applications", null));
                    Request request = data.getFirst();
                    Response response = data.getSecond();
                    Result<byte[], FuelError> obj = data.getThird();
                    System.out.println(new String(obj.get()));
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
