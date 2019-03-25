package a1;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;


public class ActionToggleDolphin extends AbstractInputAction {
    private Camera camera;
    private SceneNode node;

    private static Vector3f cameraPos;


    public ActionToggleDolphin(Camera c, SceneNode dolphinNode) {
        camera = c;
        node = dolphinNode;

    }

    public void performAction(float time, Event event) {
        //node.detachObject("cameraNode");
        if (camera.getMode() == 'r') {

            camera.setMode('c');


            camera.setPo((Vector3f) node.getLocalPosition());


            Vector3f dolphinLoc = (Vector3f) node.getLocalPosition();
            Vector3f cf = (Vector3f) camera.getFd();
            Vector3f cu = (Vector3f) camera.getUp();
            Vector3f cr = (Vector3f) camera.getRt();


            camera.setFd((Vector3f) (node.getWorldForwardAxis().normalize()).mult(-1.0f));
            camera.setRt((Vector3f) node.getWorldRightAxis().normalize());
            camera.setUp((Vector3f) node.getWorldUpAxis().normalize());

            cameraPos = (Vector3f) camera.getRt();
            Vector3f n = camera.getFd();
            Vector3f v = camera.getUp();
            Vector3f u = camera.getRt();
            Vector3f p = camera.getPo();
            //move to left of dolphin
            Vector3f p1 = (Vector3f) Vector3f.createFrom(-0.5f * u.x(), -0.5f * u.y(), -0.5f * u.z());
            Vector3f p2 = (Vector3f) p.add((Vector3) p1);
            camera.setPo((Vector3f) Vector3f.createFrom(p2.x(), p2.y(), p2.z()));

            //rotate camera in forward direction of dolphin
            Vector3f newU = (Vector3f) u.rotate(Degreef.createFrom(-180.0f), v);
            Vector3f newN = (Vector3f) n.rotate(Degreef.createFrom(-180.0f), v);
            camera.setRt(newU);
            camera.setFd(newN);


        } else if (camera.getMode() == 'c') {
            System.out.println("dolphin is in C mode");

            camera.setMode('r');


        }


    }

}

