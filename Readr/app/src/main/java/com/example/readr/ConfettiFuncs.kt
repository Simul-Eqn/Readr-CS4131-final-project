// adapted from: https://github.com/CuriousNikhil/compose-particle-system/blob/main/app/src/main/java/me/nikhilchaudhari/compose_particle_system/MainActivity.kt

package com.example.readr

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import me.nikhilchaudhari.quarks.CreateParticles
import me.nikhilchaudhari.quarks.core.PI
import me.nikhilchaudhari.quarks.particle.Acceleration
import me.nikhilchaudhari.quarks.particle.EmissionType
import me.nikhilchaudhari.quarks.particle.Force
import me.nikhilchaudhari.quarks.particle.LifeTime
import me.nikhilchaudhari.quarks.particle.ParticleColor
import me.nikhilchaudhari.quarks.particle.ParticleSize
import me.nikhilchaudhari.quarks.particle.Velocity

@Composable
fun Fountain(x:Float=500f, y:Float=2000f, mod:Modifier = Modifier) {
    CreateParticles(
        modifier = mod
            .fillMaxSize(),
        x = x, y = y,
        velocity = Velocity(xDirection = 1f, yDirection = -15f, angle = PI, randomize = true),
        force = Force.Gravity(0.2f),
        acceleration = Acceleration(0f, -4f),
        particleSize = ParticleSize.RandomSizes(10..20),
        particleColor = ParticleColor.RandomColors(listOf(Color.Blue, Color.Cyan)),
        lifeTime = LifeTime(255f, 1f),
        emissionType = EmissionType.FlowEmission(maxParticlesCount = 500),
        durationMillis = 10 * 1000
    )
}

@Composable
fun Confetti(x:Float=500f, y:Float=200f, mod:Modifier = Modifier) {
    CreateParticles(
        modifier = mod
            .fillMaxSize(),
        x = x, y = y,
        velocity = Velocity(xDirection = 2f, yDirection = -2f, randomize = true),
        force = Force.Gravity(0.3f),
        acceleration = Acceleration(),
        particleSize = ParticleSize.RandomSizes(20..60),
        particleColor = ParticleColor.RandomColors(listOf(Color.Yellow, Color.Blue, Color.Red, Color.White, Color.Magenta, Color.Green)),
        lifeTime = LifeTime(255f, 2f),
        emissionType = EmissionType.FlowEmission(maxParticlesCount = EmissionType.FlowEmission.INDEFINITE, emissionRate = 0.8f),
        durationMillis = 1000
    )
}
