import React from "react";
import { WrongLocation } from "./WrongLocation";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "WrongLocation",
  component: WrongLocation,
};

export const Default = () => <WrongLocation />;
export const Fill = () => <WrongLocation fill="blue" />;
export const Size = () => <WrongLocation height="50" width="50" />;
export const CustomStyle = () => <WrongLocation style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <WrongLocation className="custom-class" />;
export const Clickable = () => <WrongLocation onClick={()=>console.log("clicked")} />;

const Template = (args) => <WrongLocation {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
