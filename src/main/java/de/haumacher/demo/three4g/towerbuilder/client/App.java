package de.haumacher.demo.three4g.towerbuilder.client;

import org.treblereel.gwt.three4g.cameras.PerspectiveCamera;
import org.treblereel.gwt.three4g.core.BufferGeometry;
import org.treblereel.gwt.three4g.core.Object3D;
import org.treblereel.gwt.three4g.geometries.BoxGeometry;
import org.treblereel.gwt.three4g.lights.AmbientLight;
import org.treblereel.gwt.three4g.lights.DirectionalLight;
import org.treblereel.gwt.three4g.materials.LineBasicMaterial;
import org.treblereel.gwt.three4g.materials.MeshPhongMaterial;
import org.treblereel.gwt.three4g.materials.parameters.LineBasicMaterialParameters;
import org.treblereel.gwt.three4g.materials.parameters.MeshPhongMaterialParameters;
import org.treblereel.gwt.three4g.math.Color;
import org.treblereel.gwt.three4g.math.Vector3;
import org.treblereel.gwt.three4g.objects.Line;
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
 */
public class App implements EntryPoint {
	private Scene _scene;

	private Mesh _cube;

	private PerspectiveCamera _camera;

	private WebGLRenderer _renderer;
	
	private float _targetX1 = -3;
	private float _targetX2 = 3;
	private float _targetY1 = -3;
	private float _targetY2 = 3;

	private Direction _direction = Direction.TO_FRONT;

	private Object3D _tower;

	private boolean _stop;

	private float _hue = 0.0f;

	private Mesh _fragment;

	private double _currentTime;

	private double _fragmentTimeOffset;

	private float _fragmentSpeed;
	
	static final float BOX_DEPTH = 0.5f;

	private static final float MIN_FRAGMENT = 0.1f;

	@Override
	public void onModuleLoad() {
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
		
		// Create a coordinate system for debugging.
		if (false) {
			_scene.add(line(v(0, 0, 10), v(0,0,-10), 0xFF0000));
			_scene.add(line(v(-10, 0, 0), v(10,0,0), 0x0000FF));
			_scene.add(line(v(0, -10, 0), v(0,10,0), 0x00FF00));
		}

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
		
		float targetWidth = _targetX2 - _targetX1;
		float targetHeight = _targetY2 - _targetY1;
		BoxGeometry targetGeometry = new BoxGeometry(targetWidth, targetHeight, BOX_DEPTH);
		Mesh target = new Mesh(targetGeometry, createMaterial());
		target.position.set(0, 0, -BOX_DEPTH);
		_tower.add(target);
		
		createCube();

		// Start the event loop by requesting the first update.
		DomGlobal.requestAnimationFrame(this::startEvent);
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
		_boxWidth = _targetX2 - _targetX1;
		_boxHeight = _targetY2 - _targetY1;
		BoxGeometry boxGeometry = new BoxGeometry(_boxWidth, _boxHeight, BOX_DEPTH);
		_cube = new Mesh(boxGeometry, createMaterial());
		if (_direction == Direction.TO_FRONT) {
			_cube.position.set(_targetX1 + _boxWidth / 2, 8, 0);
		} else {
			_cube.position.set(-8, _targetY1 + _boxHeight / 2, 0);
		}
		_scene.add(_cube);
	}

	private static Object3D line(Vector3 from, Vector3 to, int color) {
		return new Line(new BufferGeometry<>().setFromPoints(points(from, to)), lineBasicMaterial(color));
	}

	private static LineBasicMaterial lineBasicMaterial(int color) {
		LineBasicMaterialParameters parameters = new LineBasicMaterialParameters();
		parameters.color = new Color(color);
		return new LineBasicMaterial(parameters);
	}

	private static Vector3 v(int x, int y, int z) {
		return new Vector3(x, y, z);
	}

	private static Vector3[] points(Vector3 ...vs) {
		return vs;
	}
	
	void onKeyDown(Event event) {
		_scene.remove(_cube);
		
		float cubeX1 = _cube.position.x - _boxWidth / 2;
		float cubeX2 = _cube.position.x + _boxWidth / 2;
		float cubeY1 = _cube.position.y - _boxHeight / 2;
		float cubeY2 = _cube.position.y + _boxHeight / 2;

		float matchX1 = Math.max(_targetX1, cubeX1);
		float matchX2 = Math.min(_targetX2, cubeX2);
		float matchY1 = Math.max(_targetY1, cubeY1);
		float matchY2 = Math.min(_targetY2, cubeY2);
		
		float matchW = matchX2 - matchX1;
		float matchH = matchY2 - matchY1;
		
		if (matchW <= 0.0 || matchH <= 0.0) {
			_stop = true;
			return;
		}
		
		float matchX = matchX1 + matchW /2;
		float matchY = matchY1 + matchH /2;
		
		if (_direction == Direction.TO_FRONT) {
			float fragmentSize = _cube.position.y - (_targetY1 + _targetY2) / 2;
			if (Math.abs(fragmentSize) < MIN_FRAGMENT) {
				matchY1 = _targetY1;
				matchY2 = _targetY2;
			} else {
				float fragmentY;
				if (fragmentSize > 0) {
					// Behind the tower.
					fragmentY = _targetY2 + fragmentSize / 2;
				} else {
					// In front of the tower.
					fragmentY = _targetY1 + fragmentSize / 2;
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
			float fragmentSize = _cube.position.x - (_targetX1 + _targetX2) / 2;
			if (Math.abs(fragmentSize) < MIN_FRAGMENT) {
				matchX1 = _targetX1;
				matchX2 = _targetX2;
			} else {
				float fragmentX;
				if (fragmentSize > 0) {
					// At the right.
					fragmentX = _targetX2 + fragmentSize / 2;
				} else {
					// At the left.
					fragmentX = _targetX1 + fragmentSize / 2;
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
		
		BoxGeometry matchGeometry = new BoxGeometry(matchW, matchH, BOX_DEPTH);
		Mesh matchCube = new Mesh(matchGeometry, _cube.material);
		matchCube.position.set(matchX, matchY, -_tower.position.z);
		_tower.add(matchCube);
		_tower.position.z -= BOX_DEPTH;

		_targetX1 = matchX1;
		_targetX2 = matchX2;
		_targetY1 = matchY1;
		_targetY2 = matchY2;
		
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

	private float _boxWidth;

	private float _boxHeight;
	
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
		
		if (_direction == Direction.TO_FRONT) {
			_cube.position.y = 10 - deltaY;
		} else {
			_cube.position.x = -10 + deltaY;
		}
		
		if (_fragment != null) {
			float deltaT = (float) (timestamp - _fragmentTimeOffset);
			_fragmentSpeed += FRAGMENT_ACCELERATION * deltaT;
			_fragment.position.z -= _fragmentSpeed;
			if (_fragment.position.z < -10) {
				clearFragment();
			}
		}

		// Render the updated scene.
		_renderer.render(_scene, _camera);
	}

}
