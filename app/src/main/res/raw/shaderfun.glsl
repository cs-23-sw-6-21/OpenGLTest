#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform vec2 resolution;
uniform sampler2D framebuffer;
uniform vec2 framebuffer_resolution;
uniform sampler2D noise;
uniform float time;

void main() {
    vec2 fbUV = gl_FragCoord.xy / framebuffer_resolution;
    vec2 noiseUV = gl_FragCoord.xy / 1024.0 / 5.0 + time / 4.0;


    vec4 noiseColor = texture2D(noise, noiseUV);

    vec2 uv1 = (noiseColor.xy -0.5);
    vec4 noiseColor2 = texture2D(noise, uv1);

    //uv = vec2(uv.x + sin(uv.y * 20.0) / 20.0, uv.y );
    vec4 inputColor = texture2D(framebuffer, fbUV + (noiseColor2.xy -0.5)*0.2);

    //gl_FragColor = vec4(noiseColor2.xy,0.0,1.0);
    gl_FragColor = inputColor;
    //gl_FragColor = texture2D(noise, uv);
}