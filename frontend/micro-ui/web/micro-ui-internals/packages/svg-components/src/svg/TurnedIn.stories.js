import React from "react";
import { TurnedIn } from "./TurnedIn";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "TurnedIn",
  component: TurnedIn,
};

export const Default = () => <TurnedIn />;
export const Fill = () => <TurnedIn fill="blue" />;
export const Size = () => <TurnedIn height="50" width="50" />;
export const CustomStyle = () => <TurnedIn style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TurnedIn className="custom-class" />;
export const Clickable = () => <TurnedIn onClick={()=>console.log("clicked")} />;

const Template = (args) => <TurnedIn {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
