package fr.bmartel.bboxapi.javasample;

import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;
import com.github.kittinunf.result.Result;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.mannodermaus.rxbonjour.platforms.desktop.DesktopPlatform;
import fr.bmartel.bboxapi.stb.BboxApiStb;
import fr.bmartel.bboxapi.stb.IWebsocketListener;
import fr.bmartel.bboxapi.stb.model.AppEvent;
import fr.bmartel.bboxapi.stb.model.BboxApiError;
import fr.bmartel.bboxapi.stb.model.MediaEvent;
import fr.bmartel.bboxapi.stb.model.MessageEvent;
import fr.bmartel.bboxapi.stb.model.NotificationChannel;
import fr.bmartel.bboxapi.stb.model.Resource;
import kotlin.Triple;
import kotlin.Unit;

public class Notifications {

    public static void main(String args[]) {
        BboxApiStb bboxapi = new BboxApiStb("YourAppId", "YourAppSecret");

        bboxapi.startRestDiscovery(true, DesktopPlatform.create(), 25000, (stbServiceEvent, stbService, changed, throwable) -> {
            switch (stbServiceEvent) {
                case SERVICE_FOUND:
                    System.out.println("service found : " + stbService.getIp() + ":" + stbService.getPort());

                    //unsubscribe all channels
                    System.out.println(bboxapi.unsubscribeAllSync());

                    List<Resource> resourceList = new ArrayList<>();
                    resourceList.add(Resource.Application);
                    resourceList.add(Resource.Media);
                    resourceList.add(Resource.Message);

                    NotificationChannel notificationChannel = bboxapi.subscribeNotification(
                            "myApplication",
                            resourceList,
                            new IWebsocketListener() {
                                @Override
                                public void onOpen() {
                                    System.out.println("websocket opened");
                                }

                                @Override
                                public void onClose() {
                                    System.out.println("websocket closed");
                                }

                                @Override
                                public void onError(@NotNull BboxApiError error) {
                                    System.out.println("error : " + error);
                                }

                                @Override
                                public void onMedia(@NotNull MediaEvent media) {
                                    System.out.println("channel change event : " + media);
                                }

                                @Override
                                public void onApp(@NotNull AppEvent app) {
                                    System.out.println("application event : " + app);
                                }

                                @Override
                                public void onMessage(@NotNull MessageEvent message) {
                                    System.out.println("message event : " + message);
                                }

                                @Override
                                public void onFailure(@Nullable Throwable throwable) {
                                    throwable.printStackTrace();
                                }
                            });

                    Triple<Request, Response, Result<byte[], FuelError>> result = notificationChannel.getSubscribeResult();

                    if (result.component3().component2() != null) {
                        result.component3().component2().printStackTrace();
                    } else {
                        System.out.println("subscribed with resource on channelId " +
                                notificationChannel.getChannelId() +
                                " & appId " + notificationChannel.getAppId());
                    }

                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {

                            Triple<Request, Response, Result<byte[], FuelError>> result = bboxapi.sendNotificationSync(
                                    notificationChannel.getChannelId(),
                                    notificationChannel.getAppId(),
                                    "some message"
                            );
                            if (result.component3().component2() != null) {
                                result.component3().component2().printStackTrace();
                            } else {
                                System.out.println("message sent");
                            }

                            Triple<Request, Response, Result<byte[], FuelError>> result2 = bboxapi.sendNotificationSync(
                                    notificationChannel.getChannelId(),
                                    notificationChannel.getAppId(),
                                    "some message"
                            );
                            if (result2.component3().component2() != null) {
                                result2.component3().component2().printStackTrace();
                            } else {
                                System.out.println("message sent");
                            }
                        }
                    }, 2000);

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
