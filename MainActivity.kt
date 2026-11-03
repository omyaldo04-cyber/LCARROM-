package com.lcarrom.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CarromApp() }
    }
}

private data class Coin(var x: Float, var y: Float, var vx: Float = 0f, var vy: Float = 0f,
                        val r: Float = 0.027f, val kind: Int = 0, var pocketed: Boolean = false)

@Composable
private fun CarromApp() {
    var mode by remember { mutableStateOf("2 PLAYER") }
    var turn by remember { mutableIntStateOf(1) }
    var score1 by remember { mutableIntStateOf(0) }
    var score2 by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("Striker ko drag karke aim karein, phir chhodein.") }
    var coins by remember { mutableStateOf(newCoins()) }
    var strikerX by remember { mutableFloatStateOf(.5f) }
    var aim by remember { mutableStateOf(Offset(.5f, .72f)) }
    var power by remember { mutableFloatStateOf(.65f) }
    var moving by remember { mutableStateOf(false) }

    LaunchedEffect(coins, moving) {
        while (true) {
            if (moving) {
                var any = false
                coins.forEach { c ->
                    if (!c.pocketed) {
                        c.x += c.vx; c.y += c.vy
                        c.vx *= .986f; c.vy *= .986f
                        if (abs(c.vx) + abs(c.vy) < .0007f) { c.vx = 0f; c.vy = 0f }
                        if (c.x < .12f || c.x > .88f) { c.vx *= -.88f; c.x = c.x.coerceIn(.12f,.88f) }
                        if (c.y < .12f || c.y > .88f) { c.vy *= -.88f; c.y = c.y.coerceIn(.12f,.88f) }
                        if ((c.x-.5f).pow(2)+(c.y-.5f).pow(2) > 0f) {
                            val dx=c.x-.5f; val dy=c.y-.5f
                            if (dx*dx+dy*dy < .00001f) { c.vx *= -1; c.vy *= -1 }
                        }
                        // Corner pockets
                        val pockets = listOf(Offset(.105f,.105f),Offset(.895f,.105f),Offset(.105f,.895f),Offset(.895f,.895f))
                        if (pockets.any { (it.x-c.x).pow(2)+(it.y-c.y).pow(2) < .0011f }) {
                            c.pocketed=true
                            if (c.kind == 1) score1 += 1 else if (c.kind == 2) score2 += 1
                        }
                        any = any || abs(c.vx)+abs(c.vy) > .0001f
                    }
                }
                // Simple coin-to-coin collision
                for (i in coins.indices) for (j in i+1 until coins.size) {
                    val a=coins[i]; val b=coins[j]
                    if (!a.pocketed && !b.pocketed) {
                        val dx=b.x-a.x; val dy=b.y-a.y; val d=sqrt(dx*dx+dy*dy)
                        val minD=a.r+b.r
                        if (d in .0001f..minD) {
                            val nx=dx/d; val ny=dy/d
                            val rel=(a.vx-b.vx)*nx+(a.vy-b.vy)*ny
                            if (rel>0) { a.vx-=rel*nx; a.vy-=rel*ny; b.vx+=rel*nx; b.vy+=rel*ny }
                            val overlap=(minD-d)/2
                            a.x-=nx*overlap; a.y-=ny*overlap; b.x+=nx*overlap; b.y+=ny*overlap
                        }
                    }
                }
                if (!any) {
                    moving=false
                    turn=if(turn==1) 2 else 1
                    message="Player $turn ki baari."
                }
            }
            delay(16)
        }
    }

    MaterialTheme {
        Column(Modifier.fillMaxSize().background(Color(0xFF171C22)).padding(16.dp),
            horizontalAlignment=Alignment.CenterHorizontally) {
            Text("L CARROM", color=Color(0xFFFFD36A), fontSize=30.sp, fontWeight=FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) {
                Text("PLAYER 1: $score1", color=Color.White, fontWeight=FontWeight.Bold)
                Text("PLAYER 2: $score2", color=Color.White, fontWeight=FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                listOf("2 PLAYER","VS COMPUTER").forEach { item ->
                    Button(onClick={ mode=item; resetGame { c,s -> coins=c; strikerX=.5f; score1=0; score2=0; turn=1; moving=false; message="New game shuru!" } },
                        colors=ButtonDefaults.buttonColors(containerColor=if(mode==item) Color(0xFFB8792D) else Color.DarkGray)) { Text(item) }
                }
            }
            Spacer(Modifier.height(10.dp))
            BoxWithConstraints(Modifier.fillMaxWidth().weight(1f), contentAlignment=Alignment.Center) {
                val boardSize = minOf(maxWidth, maxHeight)
                Canvas(Modifier.size(boardSize).pointerInput(moving, strikerX, aim) {
                    detectDragGestures(onDragEnd = {
                        if (!moving) {
                            val dx = strikerX-aim.x; val dy=.82f-aim.y
                            val p=(sqrt(dx*dx+dy*dy)*1.8f).coerceIn(.12f,1.0f)
                            coins.filter { !it.pocketed }.forEach { it.vx=dx*p*.075f; it.vy=dy*p*.075f }
                            moving=true; message="Shot!"
                        }
                    }) { change, _ ->
                        val x=(change.position.x/size.width).coerceIn(.15f,.85f)
                        val y=(change.position.y/size.height).coerceIn(.68f,.85f)
                        strikerX=x; aim=Offset(x+(x-.5f)*.5f,.72f)
                    }
                }.pointerInput(moving) {
                    detectTapGestures {
                        if (!moving) {
                            coins.filter { !it.pocketed }.forEach { it.vx=(.5f-it.x)*.035f; it.vy=(.5f-it.y)*.035f }
                            moving=true
                        }
                    }
                }) {
                    val w=size.width; val h=size.height
                    drawRect(Color(0xFF9B572B))
                    drawRect(Color(0xFFE5B879), Offset(w*.055f,h*.055f), androidx.compose.ui.geometry.Size(w*.89f,h*.89f))
                    drawRect(Color(0xFF6E351D), Offset(w*.085f,h*.085f), androidx.compose.ui.geometry.Size(w*.83f,h*.83f), style=androidx.compose.ui.graphics.drawscope.Stroke(5f))
                    drawRect(Color(0xFFF2D5A0), Offset(w*.12f,h*.12f), androidx.compose.ui.geometry.Size(w*.76f,h*.76f))
                    val center=Offset(w*.5f,h*.5f)
                    drawCircle(Color(0xFFB65C36), w*.105f, center, style=androidx.compose.ui.graphics.drawscope.Stroke(5f))
                    drawCircle(Color(0xFFB65C36), w*.025f, center)
                    listOf(Offset(.105f,.105f),Offset(.895f,.105f),Offset(.105f,.895f),Offset(.895f,.895f)).forEach {
                        drawCircle(Color(0xFF291711),w*.045f,Offset(w*it.x,h*it.y))
                    }
                    // Baselines and striker placement
                    drawLine(Color(0xFF9A4D2B),Offset(w*.22f,h*.82f),Offset(w*.78f,h*.82f),6f)
                    drawLine(Color(0xFF9A4D2B),Offset(w*.22f,h*.18f),Offset(w*.78f,h*.18f),6f)
                    coins.filter { !it.pocketed }.forEach { c ->
                        val color=when(c.kind){1->Color(0xFFF5E8D0);2->Color(0xFF242424);else->Color(0xFFC62828)}
                        drawCircle(Color.Black.copy(alpha=.25f),c.r*w*1.12f,Offset(c.x*w+2,c.y*h+3))
                        drawCircle(color,c.r*w,Offset(c.x*w,c.y*h))
                        drawCircle(Color.White.copy(alpha=.5f),c.r*w*.35f,Offset(c.x*w-c.r*w*.25f,c.y*h-c.r*h*.25f))
                    }
                    drawCircle(Color(0xFF2F7CC4),w*.035f,Offset(strikerX*w,h*.82f))
                    drawCircle(Color.White,w*.035f,Offset(strikerX*w,h*.82f),style=androidx.compose.ui.graphics.drawscope.Stroke(3f))
                }
            }
            Text(message,color=Color(0xFFFFD36A),fontSize=13.sp)
            Spacer(Modifier.height(8.dp))
            Button(onClick={
                if (!moving) {
                    val dx=aim.x-strikerX; val dy=aim.y-.82f
                    val p=power*.09f
                    coins.filter { !it.pocketed }.forEach { c -> c.vx += -dx*p; c.vy += -dy*p }
                    moving=true
                }
            }, enabled=!moving, colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFB8792D)), modifier=Modifier.fillMaxWidth()) {
                Text("SHOOT", fontWeight=FontWeight.Bold)
            }
            Slider(value=power,onValueChange={power=it}, enabled=!moving)
            Text("POWER",color=Color.LightGray,fontSize=11.sp)
            Text("Tip: board par striker ko drag karke shot direction set karein.",color=Color.LightGray,fontSize=11.sp)
            TextButton(onClick={ coins=newCoins(); score1=0;score2=0;turn=1;moving=false;message="Game reset ho gaya." }) { Text("NEW GAME",color=Color(0xFFFFD36A)) }
        }
    }
}

private fun newCoins(): MutableList<Coin> {
    val list= mutableListOf<Coin>()
    list.add(Coin(.5f,.5f,kind=0,r=.032f))
    val positions=listOf(
        .5f to .425f, .565f to .462f, .565f to .538f, .5f to .575f,
        .435f to .538f, .435f to .462f, .5f to .35f, .63f to .5f,
        .5f to .65f, .37f to .5f
    )
    positions.forEachIndexed { i,p -> list.add(Coin(p.first,p.second,kind=if(i%2==0)1 else 2)) }
    return list
}
private inline fun resetGame(update:(MutableList<Coin>,Int)->Unit) { update(newCoins(),0) }
