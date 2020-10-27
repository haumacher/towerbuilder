package de.haumacher.demo.three4g.towerbuilder.client;

import org.treblereel.gwt.three4g.cameras.PerspectiveCamera;
import org.treblereel.gwt.three4g.core.Object3D;
import org.treblereel.gwt.three4g.geometries.BoxGeometry;
import org.treblereel.gwt.three4g.lights.AmbientLight;
import org.treblereel.gwt.three4g.lights.DirectionalLight;
import org.treblereel.gwt.three4g.materials.MeshPhongMaterial;
import org.treblereel.gwt.three4g.materials.parameters.MeshPhongMaterialParameters;
import org.treblereel.gwt.three4g.math.Color;
import org.treblereel.gwt.three4g.math.Vector3;
import org.treblereel.gwt.three4g.objects.Mesh;
import org.treblereel.gwt.three4g.renderers.WebGLRenderer;
import org.treblereel.gwt.three4g.renderers.parameters.WebGLRendererParameters;
import org.treblereel.gwt.three4g.scenes.Scene;

import com.google.gwt.core.client.EntryPoint;

import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.HTMLCanvasElement;

/**
 * {@link EntryPoint} to the hello cube app.
 * 
 * <p>
 * Straight forward translation of the rotating cube example from
 * https://threejsfundamentals.org/threejs/lessons/threejs-fundamentals.html to
 * GWT using the three4g API.
 * </p>
 * 
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 */
public class App implements EntryPoint {
	private Scene _scene;

	private Mesh _cube;

	private PerspectiveCamera _camera;

	private WebGLRenderer _renderer;

	private Tower _towerModel = new Tower();
	
	private Direction _direction = Direction.BACK_TO_FRONT;

	private Object3D _tower;

	private boolean _stop;

	private float _hue = 0.0f;

	private Mesh _fragment;

	private double _currentTime;

	private double _fragmentTimeOffset;

	private float _fragmentSpeed;

	private float _fragmentBottom;
	
	static final float BOX_DEPTH = 0.5f;

	private static final float MIN_FRAGMENT = 0.1f;

	@Override
	public void onModuleLoad() {
		_towerModel.add(new Platform(-3, 3, -3, 3));
		HTMLCanvasElement canvas = 
			(HTMLCanvasElement) DomGlobal.document.getElementById("canvas");

		DomGlobal.document.addEventListener("keydown", this::onKeyDown);
		
		WebGLRendererParameters rendererParams = new WebGLRendererParameters();
		rendererParams.canvas = canvas;
		_renderer = new WebGLRenderer(rendererParams);

		// Create camera.
		float fov = 75;
		float aspect = 1.333f;
		float near = 2f;
		float far = 23;
		_camera = new PerspectiveCamera(fov, aspect, near, far);
		_camera.position.set(5, -8, 2);
		_camera.rotateOnWorldAxis(v(1,0,0), (float)((Math.PI / 2) -Math.atan2(2, 8)));
		_camera.rotateOnWorldAxis(v(0,0,1), (float) Math.atan2(5, 8));
		_camera.updateProjectionMatrix();

		_scene = new Scene();
		
		// Create lights.
		{
			int color = 0xFFFFFF;
			float intensity = 1;
			DirectionalLight light = new DirectionalLight(color, intensity);
			light.position.set(10, 10, 5);
			_scene.add(light);
		}
		
		{
			int color = 0xFFFFFF;
			float intensity = 0.5f;
			AmbientLight light2 = new AmbientLight(color, intensity);
			_scene.add(light2);		
		}

		// Create target platform
		_tower = new Object3D();
		_scene.add(_tower);
		
		Platform top = top();
		float targetWidth = top.width();
		float targetHeight = top.height();
		BoxGeometry targetGeometry = new BoxGeometry(targetWidth, targetHeight, BOX_DEPTH);
		Mesh target = new Mesh(targetGeometry, createMaterial());
		target.position.set(0, 0, -BOX_DEPTH);
		_tower.add(target);
		
		createCube();

		// Start the event loop by requesting the first update.
		DomGlobal.requestAnimationFrame(this::startEvent);
	}
	
	Platform top() {
		return _towerModel.top();
	}

	private MeshPhongMaterial createMaterial() {
		MeshPhongMaterialParameters materialParams = new MeshPhongMaterialParameters();
		Color color = new Color();
		color.setHSL(_hue, 0.8f, 0.6f);
		materialParams.color = color;
		MeshPhongMaterial material = new MeshPhongMaterial(materialParams);
		
		_hue += 0.05f;
		if (_hue > 1.0f) {
			_hue -= 1.0f;
		}
		
		return material;
	}

	private void createCube() {
		Platform top = top();
		BoxGeometry boxGeometry = new BoxGeometry(top.width(), top.height(), BOX_DEPTH);
		_cube = new Mesh(boxGeometry, createMaterial());
		if (_direction == Direction.BACK_TO_FRONT) {
			_cube.position.set(top.centerX(), 8, 0);
		} else {
			_cube.position.set(-8, top.centerY(), 0);
		}
		_scene.add(_cube);
	}

	private static Vector3 v(int x, int y, int z) {
		return new Vector3(x, y, z);
	}

	void onKeyDown(Event event) {
		_scene.remove(_cube);
		
		Platform top = top();
		
		float boxWidth = top.width();
		float boxHeight = top.height();
		
		float cubeX1 = _cube.position.x - boxWidth / 2;
		float cubeX2 = _cube.position.x + boxWidth / 2;
		float cubeY1 = _cube.position.y - boxHeight / 2;
		float cubeY2 = _cube.position.y + boxHeight / 2;
		
		Platform match = top.intersect(new Platform(cubeX1, cubeX2, cubeY1, cubeY2));
		
		float matchW = match.width();
		float matchH = match.height();
		
		if (matchW <= 0.0 || matchH <= 0.0) {
			_stop = true;
			return;
		}
		
		float matchX = match.centerX();
		float matchY = match.centerY();
		
		Side side;
		float fragmentSize;
		if (_direction == Direction.BACK_TO_FRONT) {
			fragmentSize = _cube.position.y - top.centerY();
			if (Math.abs(fragmentSize) < MIN_FRAGMENT) {
				match = top.copy();
				side = null;
			} else {
				float fragmentY;
				if (fragmentSize > 0) {
					// Behind the tower.
					fragmentY = top.getY2() + fragmentSize / 2;
					side = Side.BACK;
				} else {
					// In front of the tower.
					fragmentY = top.getY1() + fragmentSize / 2;
					side = Side.FRONT;
				}
				
				clearFragment();
				BoxGeometry fragmentGeometry = new BoxGeometry(matchW, Math.abs(fragmentSize), BOX_DEPTH);
				_fragment = new Mesh(fragmentGeometry, _cube.material);
				_fragment.position.set(matchX, fragmentY, 0);
				_fragmentSpeed = 0.0f;
				_fragmentTimeOffset = _currentTime;
				_scene.add(_fragment);
			}
		} else {
			fragmentSize = _cube.position.x - top.centerX();
			if (Math.abs(fragmentSize) < MIN_FRAGMENT) {
				match = top.copy();
				side = null;
			} else {
				float fragmentX;
				if (fragmentSize > 0) {
					// At the right.
					fragmentX = top.getX2() + fragmentSize / 2;
					side = Side.RIGHT;
				} else {
					// At the left.
					fragmentX = top.getX1() + fragmentSize / 2;
					side = Side.LEFT;
				}
				
				clearFragment();
				BoxGeometry fragmentGeometry = new BoxGeometry(Math.abs(fragmentSize), matchH, BOX_DEPTH);
				_fragment = new Mesh(fragmentGeometry, _cube.material);
				_fragment.position.set(fragmentX, matchY, 0);
				_fragmentSpeed = 0.0f;
				_fragmentTimeOffset = _currentTime;
				_scene.add(_fragment);
			}
		}
		
		if (side != null) {
			int fragmentLevel = _towerModel.edgeLevel(side);
			if (fragmentLevel > 0) {
				_fragmentBottom = -(fragmentLevel + 1) * BOX_DEPTH;
				_towerModel.get(fragmentLevel - 1).incPosition(side, fragmentSize);
			} else {
				_fragmentBottom = -30.0f;
			}
		}
		
		BoxGeometry matchGeometry = new BoxGeometry(matchW, matchH, BOX_DEPTH);
		Mesh matchCube = new Mesh(matchGeometry, _cube.material);
		matchCube.position.set(matchX, matchY, -_tower.position.z);
		_tower.add(matchCube);
		_tower.position.z -= BOX_DEPTH;

		_towerModel.add(match);
		
		_direction = _direction.next();
		createCube();
		_boxTimeOffset += _boxTime;
		_nuberCycles = 0;
	}

	/** 
	 * Drops the currently falling fragment, if any.
	 */
	private void clearFragment() {
		if (_fragment != null) {
			_scene.remove(_fragment);
			_fragment = null;
		}
	}
	
	void startEvent(double timestamp) {
		_currentTime = timestamp;
		
		_boxTimeOffset = timestamp;
		update(timestamp);

		// Request the next update.
		DomGlobal.requestAnimationFrame(this::eventLoop);
	}
	
	void eventLoop(double timestamp) {
		if (_stop) {
			_renderer.render(_scene, _camera);
			return;
		}
		
		update(timestamp);

		// Request the next update.
		DomGlobal.requestAnimationFrame(this::eventLoop);
	}

	// The time in milliseconds when the animation started.
	double _boxTimeOffset = Double.MIN_VALUE;

	// The number of full cycles already elapsed.
	private double _nuberCycles = 0;
	
	// Number of milliseconds for one trip back and forth.
	private static final double PERIOD_MS = 5000;

	private static final float DELTA = 13;
	private static final float DELTA_2 = 2 * DELTA;

	// Acceleration of a falling fragment in units per millisecond^2
	private static final float FRAGMENT_ACCELERATION = 0.0001f;

	private double _boxTime;

	void update(double timestamp) {
		_currentTime = timestamp;
		_boxTime = timestamp - _boxTimeOffset;
		
		// A value increasing from 0 to 1. If 1 is reached, starts over at 0.
		double n = (_boxTime / PERIOD_MS) - _nuberCycles;
		if (n > 1.0) {
			n -= 1.0;
			_nuberCycles = _nuberCycles + 1.0;
		}
		
		float deltaY = (float) (DELTA_2 * n);
		if (deltaY > DELTA) {
			deltaY = DELTA_2 - deltaY;
		}
		
		if (_direction == Direction.BACK_TO_FRONT) {
			_cube.position.y = 10 - deltaY;
		} else {
			_cube.position.x = -10 + deltaY;
		}
		
		if (_fragment != null) {
			float deltaT = (float) (timestamp - _fragmentTimeOffset);
			_fragmentSpeed += FRAGMENT_ACCELERATION * deltaT;
			_fragment.position.z -= _fragmentSpeed;
			if (_fragment.position.z < _fragmentBottom) {
				_scene.remove(_fragment);
				
				_fragment.position.z = _fragmentBottom - _tower.position.z;
				_tower.add(_fragment);
				_fragment = null;
				
				clearFragment();
			} else if (_fragment.position.z < -20) {
				clearFragment();
			}
		}

		// Render the updated scene.
		_renderer.render(_scene, _camera);
	}

}
