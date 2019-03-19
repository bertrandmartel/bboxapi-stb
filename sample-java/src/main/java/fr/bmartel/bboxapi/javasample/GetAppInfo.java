package fr.bmartel.bboxapi.javasample;

import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Handler;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;
import com.github.kittinunf.result.Result;
import de.mannodermaus.rxbonjour.platforms.desktop.DesktopPlatform;
import fr.bmartel.bboxapi.stb.BboxApiStb;
import fr.bmartel.bboxapi.stb.model.Application;
import kotlin.Triple;
import kotlin.Unit;

import java.util.List;

public class GetAppInfo {

    public static void main(String args[]) {
        BboxApiStb bboxapi = new BboxApiStb("YourAppId", "YourAppSecret");

        bboxapi.startRestDiscovery(true, DesktopPlatform.create(), 10000, (stbServiceEvent, stbService, changed, throwable) -> {
            switch (stbServiceEvent) {
                case SERVICE_FOUND:
                    System.out.println("service found : " + stbService.getIp() + ":" + stbService.getPort());
                    Triple<Request, Response, Result<List<Application>, FuelError>> data = bboxapi.getAppInfoSync("com.google.android.youtube.tv");
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
