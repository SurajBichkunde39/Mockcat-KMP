package com.mockcat.logger.ui

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import platform.CoreGraphics.CGPoint
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSNotificationCenter
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UIEvent
import platform.UIKit.UIFont
import platform.UIKit.UILabel
import platform.UIKit.UIScreen
import platform.UIKit.UITouch
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowLevelAlert
import platform.UIKit.UIWindowScene

private const val OPEN_LOGGER_NOTIFICATION = "MockcatOpenLogger"
private const val BUTTON_SIZE = 64.0
private const val BUTTON_MARGIN = 20.0

@OptIn(ExperimentalForeignApi::class)
internal object HttpLogOverlayIos {
    private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var overlayWindow: PassThroughWindow? = null

    fun show() {
        mainScope.launch {
            ensureWindow()
            overlayWindow?.setHidden(false)
        }
    }

    fun hide() {
        mainScope.launch {
            overlayWindow?.setHidden(true)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun ensureWindow() {
        if (overlayWindow != null) return

        val screen = UIScreen.mainScreen
        val screenBounds = screen.bounds
        val screenWidth = screenBounds.useContents { size.width }
        val screenHeight = screenBounds.useContents { size.height }

        val window = PassThroughWindow(frame = screenBounds)
        // Associate with the active scene so the window is visible on iOS 13+.
        (UIApplication.sharedApplication.connectedScenes as Set<*>)
            .filterIsInstance<UIWindowScene>()
            .firstOrNull()
            ?.let { window.windowScene = it }
        window.windowLevel = UIWindowLevelAlert + 1.0
        window.backgroundColor = UIColor.clearColor
        window.setHidden(true)

        val rootVC = PassThroughViewController()
        window.rootViewController = rootVC

        val button = OverlayButton(
            frame = CGRectMake(
                screenWidth - BUTTON_SIZE - BUTTON_MARGIN,
                screenHeight - BUTTON_SIZE - BUTTON_MARGIN - 80.0,
                BUTTON_SIZE,
                BUTTON_SIZE,
            ),
        )
        button.onTap = {
            // Same notification ContentView already listens to — no host-app changes needed.
            NSNotificationCenter.defaultCenter()
                .postNotificationName(OPEN_LOGGER_NOTIFICATION, null, null)
        }
        rootVC.view.addSubview(button)

        overlayWindow = window
    }
}

// UIWindow subclass that passes through touches on empty areas.
// Without this, UIWindow.hitTest falls back to returning itself when all subviews
// return nil, which captures every touch and blocks the app below.
@OptIn(ExperimentalForeignApi::class)
private class PassThroughWindow(frame: CValue<CGRect>) : UIWindow(frame) {
    override fun hitTest(point: CValue<CGPoint>, withEvent: UIEvent?): UIView? {
        val hit = super.hitTest(point, withEvent = withEvent)
        return if (hit == this) null else hit
    }
}

// UIView whose background passes touches through to the window below.
@OptIn(ExperimentalForeignApi::class)
private class PassThroughView(frame: CValue<CGRect>) : UIView(frame) {
    override fun hitTest(point: CValue<CGPoint>, withEvent: UIEvent?): UIView? {
        val hit = super.hitTest(point, withEvent = withEvent)
        return if (hit == this) null else hit
    }
}

private class PassThroughViewController : UIViewController(nibName = null, bundle = null) {
    @OptIn(ExperimentalForeignApi::class)
    override fun loadView() {
        view = PassThroughView(frame = UIScreen.mainScreen.bounds)
        view.backgroundColor = UIColor.clearColor
    }
}

// Draggable circular badge — tap to open the HTTP logger.
// CValue<CGPoint> cannot be stored as an ObjC class instance variable in K/N, so
// touch tracking uses separate Double fields.
@OptIn(ExperimentalForeignApi::class)
private class OverlayButton(frame: CValue<CGRect>) : UIView(frame) {
    var onTap: (() -> Unit)? = null
    private var touchStartX = 0.0
    private var touchStartY = 0.0
    private var hasMoved = false

    init {
        val w = frame.useContents { size.width }
        val h = frame.useContents { size.height }

        backgroundColor = UIColor(red = 0.05, green = 0.05, blue = 0.05, alpha = 0.88)
        layer.cornerRadius = w / 2.0
        setClipsToBounds(true)

        val label = UILabel()
        label.text = "HTTP"
        label.textColor = UIColor.whiteColor
        label.font = UIFont.boldSystemFontOfSize(12.0)
        label.textAlignment = NSTextAlignmentCenter
        label.setFrame(CGRectMake(0.0, 0.0, w, h))
        addSubview(label)
    }

    @Suppress("UNCHECKED_CAST")
    override fun touchesBegan(touches: Set<*>, withEvent: UIEvent?) {
        super.touchesBegan(touches, withEvent)
        hasMoved = false
        val loc = (touches as Set<UITouch>).first().locationInView(superview)
        touchStartX = loc.useContents { x }
        touchStartY = loc.useContents { y }
    }

    @Suppress("UNCHECKED_CAST")
    override fun touchesMoved(touches: Set<*>, withEvent: UIEvent?) {
        super.touchesMoved(touches, withEvent)
        val loc = (touches as Set<UITouch>).first().locationInView(superview)
        val locX = loc.useContents { x }
        val locY = loc.useContents { y }
        val dx = locX - touchStartX
        val dy = locY - touchStartY
        if (kotlin.math.abs(dx) > 4.0 || kotlin.math.abs(dy) > 4.0) hasMoved = true
        setCenter(CGPointMake(center.useContents { x } + dx, center.useContents { y } + dy))
        touchStartX = locX
        touchStartY = locY
    }

    @Suppress("UNCHECKED_CAST")
    override fun touchesEnded(touches: Set<*>, withEvent: UIEvent?) {
        super.touchesEnded(touches, withEvent)
        if (!hasMoved) onTap?.invoke()
    }
}
