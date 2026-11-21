import React from "react";
import { TurnedInNot } from "./TurnedInNot";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "TurnedInNot",
  component: TurnedInNot,
};

export const Default = () => <TurnedInNot />;
export const Fill = () => <TurnedInNot fill="blue" />;
export const Size = () => <TurnedInNot height="50" width="50" />;
export const CustomStyle = () => <TurnedInNot style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TurnedInNot className="custom-class" />;
export const Clickable = () => <TurnedInNot onClick={()=>console.log("clicked")} />;

const Template = (args) => <TurnedInNot {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
