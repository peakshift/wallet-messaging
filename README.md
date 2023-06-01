# Wallet Messaging
A design pattern and proof of concept for exchanging bitcoin, and arbirary payment data between apps on the same device.

## Wallet to Wallet Communication / Wallet Messaging

### Abstract
Wallet Messaging is a bitcoin design pattern for secure direct communication process between applications. The applications that exchange information can be on the same device, or separate. The latter would require a way for the applications to connect to one another remotely so would require the user to be connected to the internet, while the former can be facilitated even while offline. 

### Scope
This project will first tackle the communication between the app of a "Service Provider" which needs a payment to be made to it, for escrow, or as payment for subscription but does not wan't to accept deposits into their own wallet / application. Therefore, they need to communicate with the users wallet. Two applications will be created, one to represent the service provider, another to represent the users primary wallet. An API will be defined specifically for Service Provider <-> Wallet communication for payment details. While the scope is limited to payments for the first phase the API can be extended for additional functions like signing messages which can open functionalty for singing in via lnurl-auth or verifying a lightning payment / on-chain address ownership.

#### Rationale
There are issues that slow down or interrupt payment flows with burdensome steps for users. Wallet Messaging is a method of resolving the communication friction between bitcoin applications that need to share information with one another.

Payment procedures, such as spending from joint accounts (multi-sig), or requesting a payment require participants to be able to communicate with one another. Without a built-in method for messaging between bitcoin applications, the transacting parties are left to negotiate and coordinate themselves. For example, in the case of a multi, they would need to negotiate and establish some means to communicate that is suitable for the entire group. Then ensure everyone's bitcoin application supports the necessary functions to carry out the procedure, then start gathering the required authorizations.

Require peer-to-peer communication to complete. With Wallet Messaging, your application can clear away the hurdles and fine-tune the process so that it takes a user the least amount of steps to complete a collaborative payment procedure.

Improved communication standards between bitcoin applications

For Wallet Messaging between two separate devices that are not in near proximity - the pattern doesn't require peer-to-peer connection, but there are some benefits of using such a transport layer. While a proprietary messaging system using a central server controlled by the application developer would encourage data portability and reduced friction for different payment procedures. Anyone is free then to choose whichever bitcoin application that suits them best and not have to worry about not being able to interact on the same level with someone who is not using their chosen application.

, especially since it is not likely that all bitcoin application developers would rely on a single/the same server, especially if is a competitor. so it's better to use an open decentralized protocol for this. there is also the alignment of bitcoin principals of ownership, cutting out the middle man, and privacy that comes with using a peer-to-peer approach. 

#### Communication problems between wallets
Sending and receiving payments
To send and receive payments in Bitcoin, as it stands at the moment, users have to send each other long, complicated strings of text or file formats that aren't recognized by current operating systems. Aside from being meaningless, these bits of text are daunting to someone who is non-technical.

When a user wants to request or send a transfer, they need to copy this string and paste it into the chat window of the other person in third-party messaging applications, such as WhatsApp. This requires users to exit their Bitcoin app for a simple transaction. This is not familiar behavior as one would expect in other modern financial applications. Additionally, copying sensitive information into third-party services exposes users to potential security threats and privacy leaks.

We do not recommend sharing a private key. The whole point of the bitcoin cryptography is that everyone owns their own private key, and authorizations can be made collaboratively with each party's own key.

#### Security issues with third-party messaging applications
Since the blockchain data is public, when you copy and paste a Payment Request into a third-party messaging app, you are revealing information about your finances, not only for the payment that's about to take place but future and past transactions. Third-party messaging services can be hacked, which leaves you exposed and introduces security risks. Another attack vector in the event a transaction file (.psbt) is shared, hackers can change the payment destinations in a transaction to steal your funds, attain sensitive information about your wallet, find out the amount of money that you have, or even

