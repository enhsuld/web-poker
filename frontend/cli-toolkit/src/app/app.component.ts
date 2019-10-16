import { Component, ViewChildren } from '@angular/core';
import { RoomService } from './services/room.service';
import { TerminalService } from './services/terminal.service';
import { ApiService } from './services/api.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'cli-toolkit';

  private roomServer = '127.0.0.1:8081';
  private orchestratorServer = '127.0.0.1:8082';
  private apiServer = '127.0.0.1:8083';
  private commandPrompt: string;


  private terminalMessages = [
    'Ready'
  ];

  constructor(private room: RoomService, private terminal: TerminalService, private api: ApiService) {
    terminal.event.subscribe(data => {
      this.terminalMessages.push(data);
    });
  }

  connectRoomServer() {
    this.room.connect(this.roomServer);
  }

  keyUp(evt) {
    if(evt.keyCode === 13){
      // eval command.
      this.processCommand(this.commandPrompt);
      this.commandPrompt = '';
    }
  }

  // Command writter helper // 
  ingressCMD() {
    this.commandPrompt = 'room.ingress user:"NickName" photo:"PhotoURL" chips:"4500"';
    document.getElementById('commandPrompt').focus();
  }

  depositCMD() {
    this.commandPrompt = 'room.deposit userID:"57" coins:"500" challengeID:"16" claimToken:"xMjM0NTY3ODkwIiwibmFtZSI6Ikpva"';
    document.getElementById('commandPrompt').focus();
  }

  roomChallengeCMD() {
    this.commandPrompt = 'apisrv.challenge challengeID:"16" claimToken:"xMjM0NTY3ODkwIiwibmFtZSI6Ikpva"';
    document.getElementById('commandPrompt').focus();
  }

  backwardValidationCMD() {
    this.commandPrompt = 'room.backwardValidation challengeID:"16"';
    document.getElementById('commandPrompt').focus();
  }

  processCommand(command: string) {
    const part = /([a-zA-Z]+)\.([a-zA-Z]+)/gm.exec(command);
    const target = part[1];
    const action = part[2];
    var params: any = {};
    command.match(/[a-zA-Z]+:"[a-zA-Z0-9]+"/gm).forEach(data => {
      const result = /([a-zA-Z]+):"([a-zA-Z0-9]+)"/gm.exec(data);
      params[result[1]] = result[2];
    });
    if(target == 'room') {
      if(action == 'ingress') {
        this.room.ingress(params.user, params.photo, params.chips);
      }
      if(action == 'deposit') {
        this.room.deposit(params.userID, params.coins, params.challengeID, params.claimToken);
      }
      if(action == 'backwardValidation') {
        this.room.backwardValidation(params.challengeID);
      }
    }
    if(target == 'apisrv') {
      if(action == 'challenge') {
        // mock user challenge for test (using the first user with id = 1 hardcoded for test purposes)
        this.api.challenge(params.challengeID, params.claimToken);
      }
    }
  }
}
