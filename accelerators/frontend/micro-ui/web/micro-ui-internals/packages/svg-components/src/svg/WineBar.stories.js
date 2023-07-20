import React from "react";
import { WineBar } from "./WineBar";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "WineBar",
  component: WineBar,
};

export const Default = () => <WineBar />;
export const Fill = () => <WineBar fill="blue" />;
export const Size = () => <WineBar height="50" width="50" />;
export const CustomStyle = () => <WineBar style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <WineBar className="custom-class" />;
export const Clickable = () => <WineBar onClick={()=>console.log("clicked")} />;

const Template = (args) => <WineBar {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};