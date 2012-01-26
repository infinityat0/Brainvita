var noOfCoins   = 32 ;
var count       = 0 ;
var x0,y0 ;
var statusMessage  = "" ;
var isGameEnded = true ;
var board = new Array
                (
                    new Array(-1,-1,1,1,1,-1,-1 ),
                    new Array(-1,-1,1,1,1,-1,-1 ),
                    new Array( 1, 1,1,1,1, 1, 1 ),
                    new Array( 1, 1,1,0,1, 1, 1 ),
                    new Array( 1, 1,1,1,1, 1, 1 ),
                    new Array(-1,-1,1,1,1,-1,-1 ),
                    new Array(-1,-1,1,1,1,-1,-1 )
                ) ;

function startGame()
{   
    // here we have to initialize array.
    fillBoard();
    isGameEnded = false ;
    noOfCoins   = 32 ;
    count = 0 ;
    statusMessage = "New game Started.";
    updateStatus();
}

function fillBoard()
{
	var data = "<table cellpadding = 3 ; cellspacing = 1 >" ;
	for(var i = 0 ; i < 7 ; i++)
	{
        data = data + "<tr>" ;
		for(j = 0 ; j < 7 ; j++)
		{
            data = data + "<td><a id = '"+i+j+"' href = #>";		
			// board[i][j] = ((i>=2&&i<=4)||(j>=2&&j<=4))?1:-1
			if((i>=2&&i<=4)||(j>=2&&j<=4))
			{
				if(i == 3 && j == 3)
				{
					board[i][j] = 0 ;
					data = data + "<img src = 'images/empty.png' onclick = 'return fnClicked("+i+","+j+")'/>" ;
				}
				else
				{
					board[i][j] = 1 ;
					data = data + "<img src = 'images/coin.png' onclick = 'return fnClicked("+i+","+j+")'/>" ;
				}
			}
			else
			{
				board[i][j] = -1 ;
			}
			data = data + "</a></td>";
		}
	}
	data = data + "</table>" ;
    // alert(data);
    document.getElementById("right").innerHTML = data ;
}

function endGame()
{
    isGameEnded = true ;
    statusMessage = " You have lost.<br>." ;
    updateStatus();
}

function giveUp()
{
    statusMessage = "You loose!. Thanks for playing. " ;
    isGameEnded = true ;
    updateStatus();
}

function checkAvailableMoves()
{
    
}

function fnClicked(x,y)
{
    if(!isGameEnded)
    {    
        // alert(x+","+y) ;
        if(noOfCoins == 1)
        {
            alert("Game over. You Win");
            statusMessage  = "Game over. "
            return true ;
        }
        if(count%2 ==0)
        {
            // Count is even => move will start with this click ; 
            // >0 will also take care of -1 positions.
            if(board[x][y] > 0)
            {
                // player has chosen a coin to move. 
                // alert("avaiting jump");
                statusMessage = "avaiting jump" ;
                x0 = x ;
                y0 = y ;
                count++;
            }
            else
            {
                // nothing to do here.. 
                statusMessage = "Not a move ";
            }
        }
        else
        {
            if(board[x][y] == 1)
            {
               // player changes his coin. no need to increase count here ; 
               // alert("Changing touched piece") ;
               statusMessage = "Changing touched piece" ;
               x0 = x;
               y0 = y;
            }
            else /* this is taken for granted */ if(board[x][y] == 0 )
            {
               // player chose an empty place to jump.
               movePiece(x,y) ;
            }
        }
    }
    else
    {
        statusMessage = "Game Ended. No more play!" ;
    }
    updateStatus();
}

function updateStatus()
{
    document.getElementById('message').innerHTML = statusMessage ;
    document.getElementById('noOfMoves').innerHTML = count/2 ;
    document.getElementById('noOfCoins').innerHTML = noOfCoins ;
    
}

function movePiece(x,y)
{
    // alert("moving a piece");
                // Set 1                        // Set 2 if any one is true then fail.
    if(( (!(((x-x0)%2==0)||((y-y0)%2==0)))  ||  
            ((x-x0 == 1 )||(x0-x == 1)||(y-y0 ==1)||(y0-y ==1)) ))
    {
        // (x>x0?((x-x0)==2):((x0-x)==2))&&(y>y0?((y-y0)==2):((y0-y)==2))
        statusMessage = "Invalid move: No skipping allowed." ;
        // alert(statusMessage);
        count-- ;
        return false ;
    }
    if((x>x0?((x-x0)==2):((x0-x)==2))&&(y>y0?((y-y0)==2):((y0-y)==2)))
    {
        statusMessage = "Invalid move: No Diagonals" ;
        // alert(statusMessage);
        count--;
        return false ;
    }
    if((x>x0?((x-x0)>2):((x0-x)>2))||(y>y0?((y-y0)>2):((y0-y)>2)))
    {
        statusMessage = "Invalid move: No long jumps." ;
        // alert(statusMessage);
        count--;
        return false ;   
    }
    if(board[(x+x0)/2][(y+y0)/2] == 0)
    {
        statusMessage = "Invalid move: Can't jump over empty pieces." ;
        return false;
    }
    empty((x+x0)/2,(y+y0)/2) ;
    empty(x0,y0);
    fill(x,y);
    noOfCoins-- ;
    count++ ;
    statusMessage = "Piece Moved"
    return true ;
}

function empty(x,y)
{
    // alert("in empty");
    // alert(x+""+y);
    board[x][y] = 0 ;
    document.getElementById(x+""+y).innerHTML = "<img src = 'images/empty.png' onclick = 'return fnClicked("+x+","+y+")'/>" ;
}

function fill(x,y)
{
    // alert("in fill");
    board[x][y] = 1 ;
    document.getElementById(x+""+y).innerHTML = "<img src = 'images/coin.png' onclick = 'return fnClicked("+x+","+y+")'/>" ;
}
