import React from "react";
import { Tram } from "./Tram";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Tram",
  component: Tram,
};

export const Default = () => <Tram />;
export const Fill = () => <Tram fill="blue" />;
export const Size = () => <Tram height="50" width="50" />;
export const CustomStyle = () => <Tram style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Tram className="custom-class" />;
export const Clickable = () => <Tram onClick={()=>console.log("clicked")} />;

const Template = (args) => <Tram {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
