import UIKit

public final class SplashController: UIViewController {

	public override func viewDidLoad() {
        super.viewDidLoad()

        // Load LaunchScreen.storyboard as child VC
        let storyboard = UIStoryboard(name: "LaunchScreen", bundle: nil)

        guard let launchVC = storyboard.instantiateInitialViewController() else {
            return
        }

        addChild(launchVC)
        view.addSubview(launchVC.view)
        launchVC.view.frame = view.bounds
        launchVC.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        launchVC.didMove(toParent: self)
    }
}
